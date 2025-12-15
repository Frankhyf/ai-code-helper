package com.frank.aicodehelper.rag.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.frank.aicodehelper.rag.chunking.VueCodeChunker;
import com.frank.aicodehelper.rag.config.RagConfig;
import com.frank.aicodehelper.rag.model.CodeContextDocument;
import com.frank.aicodehelper.rag.model.ContextChunk;
import com.frank.aicodehelper.rag.service.ProjectContextService;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import redis.clients.jedis.exceptions.JedisDataException;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

/**
 * 项目上下文服务实现
 * 使用 LangChain4j 进行向量化索引和检索
 */
@Slf4j
@Service
public class ProjectContextServiceImpl implements ProjectContextService {

    @Resource
    private EmbeddingModel dashScopeEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource
    private VueCodeChunker vueCodeChunker;

    @Resource
    private RagConfig ragConfig;

    @Override
    @Async
    public void indexCodeFile(Long appId, String filePath, String content) {
        if (!isEnabled()) {
            log.debug("RAG 未启用，跳过索引");
            return;
        }

        if (StrUtil.isBlank(content)) {
            log.debug("文件内容为空，跳过索引: {}", filePath);
            return;
        }

        // 只索引支持的文件类型
        if (!vueCodeChunker.supports(filePath)) {
            log.debug("不支持的文件类型，跳过索引: {}", filePath);
            return;
        }

        try {
            // 1. 先删除该文件的旧索引
            deleteByFilePath(appId, filePath);

            // 2. 对文件进行分块
            List<CodeContextDocument> chunks = vueCodeChunker.chunk(appId, filePath, content);

            if (CollUtil.isEmpty(chunks)) {
                log.debug("分块结果为空，跳过索引: {}", filePath);
                return;
            }

            // 3. 批量向量化并存储
            for (CodeContextDocument chunk : chunks) {
                indexChunk(chunk);
            }

            log.info("✅ [RAG索引] 索引完成: appId={}, file={}, 分片数={}", appId, filePath, chunks.size());
        } catch (Exception e) {
            log.error("RAG 索引失败: appId={}, file={}, error={}",
                    appId, filePath, e.getMessage(), e);
        }
    }

    /**
     * 索引单个代码片段
     */
    private void indexChunk(CodeContextDocument chunk) {
        // 1. 生成嵌入向量
        Embedding embedding = dashScopeEmbeddingModel.embed(chunk.getContent()).content();

        // 2. 构建元数据
        Metadata metadata = new Metadata();
        metadata.put("appId", chunk.getAppId().toString());
        metadata.put("chunkId", chunk.getChunkId());
        metadata.put("filePath", chunk.getFilePath());
        metadata.put("fileType", chunk.getFileType());
        metadata.put("chunkType", chunk.getChunkType());
        metadata.put("chunkIndex", String.valueOf(chunk.getChunkIndex()));

        // 添加扩展元数据
        if (chunk.getMetadata() != null) {
            chunk.getMetadata().forEach((k, v) -> {
                if (v != null) {
                    metadata.put(k, v.toString());
                }
            });
        }

        // 3. 创建文本段
        TextSegment segment = TextSegment.from(chunk.getContent(), metadata);

        // 4. 存储到向量数据库
        embeddingStore.add(embedding, segment);

        log.debug("索引代码片段: appId={}, file={}, type={}",
                chunk.getAppId(), chunk.getFilePath(), chunk.getChunkType());
    }

    @Override
    public List<ContextChunk> searchContext(Long appId, String query, int topK, double minScore) {
        if (!isEnabled()) {
            return new ArrayList<>();
        }

        if (StrUtil.isBlank(query)) {
            return new ArrayList<>();
        }

        try {
            // 1. 将查询文本向量化
            log.debug("🔢 [RAG检索] 正在生成查询向量...");
            Embedding queryEmbedding = dashScopeEmbeddingModel.embed(query).content();
            log.debug("🔢 [RAG检索] 查询向量生成完成, 维度={}", queryEmbedding.dimension());

            // 2. 构建搜索请求（带过滤条件）
            Filter appFilter = metadataKey("appId").isEqualTo(appId.toString());

            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(topK)
                    .minScore(minScore)
                    .filter(appFilter)
                    .build();

            // 3. 执行搜索
            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

            // 4. 转换结果
            List<ContextChunk> chunks = result.matches().stream()
                    .map(this::toContextChunk)
                    .collect(Collectors.toList());

            log.info("🔎 [RAG检索] 检索完成: appId={}, query长度={}, 命中片段数={}",
                    appId, query.length(), chunks.size());

            return chunks;
        } catch (Exception e) {
            log.error("RAG 检索失败: appId={}, error={}", appId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ContextChunk> searchContext(Long appId, String query) {
        List<ContextChunk> results = searchContext(appId, query,
                ragConfig.getRetrieval().getDefaultTopK(),
                ragConfig.getRetrieval().getDefaultMinScore());
        
        // 如果阈值过滤后没有结果，且配置了保证返回 top1，则降低阈值重试
        if (results.isEmpty() && ragConfig.getRetrieval().isGuaranteeTopOne()) {
            log.info("🔄 [RAG检索] 阈值 {} 过滤后无结果，降级为返回 top1 (不限阈值)", 
                    ragConfig.getRetrieval().getDefaultMinScore());
            results = searchContext(appId, query, 1, 0.0);
            if (!results.isEmpty()) {
                log.info("✅ [RAG检索] 降级成功，返回 top1: file={}, score={}", 
                        results.get(0).getFilePath(), 
                        String.format("%.2f", results.get(0).getScore()));
            }
        }
        
        return results;
    }

    /**
     * 将 EmbeddingMatch 转换为 ContextChunk
     */
    private ContextChunk toContextChunk(EmbeddingMatch<TextSegment> match) {
        TextSegment segment = match.embedded();
        Metadata metadata = segment.metadata();

        Map<String, String> metadataMap = new HashMap<>();
        metadata.toMap().forEach((k, v) -> metadataMap.put(k, v != null ? v.toString() : null));

        return ContextChunk.builder()
                .filePath(metadata.getString("filePath"))
                .content(segment.text())
                .chunkType(metadata.getString("chunkType"))
                .score(match.score())
                .metadata(metadataMap)
                .build();
    }

    @Override
    public void deleteByAppId(Long appId) {
        if (!isEnabled()) {
            return;
        }

        try {
            // 使用过滤器删除所有该 appId 的索引
            Filter appFilter = metadataKey("appId").isEqualTo(appId.toString());
            embeddingStore.removeAll(appFilter);
            log.info("已删除应用 {} 的所有代码索引", appId);
        } catch (Exception e) {
            log.error("删除应用索引失败: appId={}, error={}", appId, e.getMessage(), e);
        }
    }

    @Override
    public void deleteByFilePath(Long appId, String filePath) {
        if (!isEnabled()) {
            return;
        }

        try {
            // 构建复合过滤条件：appId AND filePath 精确匹配
            // 分片时 filePath 已带 #fragment，因此这里使用基础路径即可避免误删其他文件
            Filter filter = metadataKey("appId").isEqualTo(appId.toString())
                    .and(metadataKey("filePath").isEqualTo(filePath));

            try {
                embeddingStore.removeAll(filter);
                log.debug("已删除文件索引: appId={}, file={}", appId, filePath);
            } catch (JedisDataException jde) {
                // Jedis 在 DEL 0 key 时会抛出“wrong number of arguments”错误，视为无匹配安全忽略
                if (jde.getMessage() != null && jde.getMessage().contains("wrong number of arguments for 'del'")) {
                    log.debug("未找到需删除的文件索引，跳过: appId={}, file={}", appId, filePath);
                } else {
                    throw jde;
                }
            }
        } catch (Exception e) {
            log.error("删除文件索引失败: appId={}, file={}, error={}",
                    appId, filePath, e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return ragConfig.isEnabled();
    }
}


