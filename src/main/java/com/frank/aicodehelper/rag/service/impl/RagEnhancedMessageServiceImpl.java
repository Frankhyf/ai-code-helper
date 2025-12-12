package com.frank.aicodehelper.rag.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.frank.aicodehelper.model.enums.CodeGenTypeEnum;
import com.frank.aicodehelper.rag.config.RagConfig;
import com.frank.aicodehelper.rag.model.ContextChunk;
import com.frank.aicodehelper.rag.service.ProjectContextService;
import com.frank.aicodehelper.rag.service.RagEnhancedMessageService;
import com.frank.aicodehelper.service.ProjectSummaryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG 消息增强服务实现
 * 将 RAG 检索结果和项目结构信息注入到用户消息中
 */
@Slf4j
@Service
public class RagEnhancedMessageServiceImpl implements RagEnhancedMessageService {

    @Resource
    private ProjectContextService projectContextService;

    @Resource
    private ProjectSummaryService projectSummaryService;

    @Resource
    private RagConfig ragConfig;

    /**
     * 单个代码片段的最大展示字符数（避免注入过多内容）
     */
    private static final int MAX_CHUNK_DISPLAY_SIZE = 2000;

    /**
     * RAG 注入的最大总字符数
     */
    private static final int MAX_RAG_TOTAL_SIZE = 8000;

    @Override
    public String enhanceMessage(String userMessage, Long appId, CodeGenTypeEnum codeGenType) {
        // 非 VUE_PROJECT 类型，使用原有的 ProjectSummaryService
        if (codeGenType != CodeGenTypeEnum.VUE_PROJECT) {
            log.info("📝 [RAG] 非 VUE_PROJECT 类型({}), 使用原有消息增强逻辑", codeGenType);
            return projectSummaryService.enhanceUserMessage(userMessage, appId, codeGenType);
        }

        // RAG 未启用，降级到原有逻辑
        if (!ragConfig.isEnabled()) {
            log.info("⚠️ [RAG] RAG 未启用 (rag.enabled=false), 降级到原有消息增强逻辑");
            return projectSummaryService.enhanceUserMessage(userMessage, appId, codeGenType);
        }

        log.info("🔍 [RAG] 开始 RAG 增强消息: appId={}, 用户消息长度={}", appId, userMessage.length());

        StringBuilder enhanced = new StringBuilder();

        // 1. 添加项目结构（复用现有 ProjectSummaryService）
        String projectSummary = projectSummaryService.generateProjectSummary(appId, codeGenType);
        if (StrUtil.isNotBlank(projectSummary)) {
            enhanced.append(projectSummary).append("\n");
            log.debug("📁 [RAG] 已注入项目结构摘要, 长度={}", projectSummary.length());
        }

        // 2. RAG 检索相关代码上下文
        log.info("🔎 [RAG] 正在进行语义检索: query=\"{}...\"", 
                userMessage.length() > 50 ? userMessage.substring(0, 50) : userMessage);
        List<ContextChunk> relevantChunks = projectContextService.searchContext(appId, userMessage);

        if (CollUtil.isNotEmpty(relevantChunks)) {
            enhanced.append("=== 相关代码上下文 (RAG检索) ===\n");
            enhanced.append("以下是与您需求语义相关的代码片段，系统已自动检索提供：\n");
            enhanced.append("⚠️ 重要：这些代码已在此提供，请直接使用，无需再调用 readFile 读取。\n\n");

            int totalSize = 0;
            for (ContextChunk chunk : relevantChunks) {
                // 控制总大小
                if (totalSize >= MAX_RAG_TOTAL_SIZE) {
                    enhanced.append("...(更多相关代码已省略)\n\n");
                    break;
                }

                String chunkContent = formatChunkForDisplay(chunk);
                totalSize += chunkContent.length();
                enhanced.append(chunkContent);
                
                log.debug("📄 [RAG] 检索到片段: file={}, score={}", 
                        chunk.getFilePath(), String.format("%.2f", chunk.getScore()));
            }

            log.info("✅ [RAG] RAG 增强完成: appId={}, 检索到 {} 个相关片段, 注入字符数={}",
                    appId, relevantChunks.size(), totalSize);
            
            // 打印检索到的文件列表
            StringBuilder fileList = new StringBuilder();
            for (ContextChunk chunk : relevantChunks) {
                String displayPath = chunk.getFilePath();
                if (displayPath.contains("#")) {
                    displayPath = displayPath.substring(0, displayPath.indexOf("#"));
                }
                fileList.append("\n    - ").append(displayPath)
                        .append(" (score: ").append(String.format("%.2f", chunk.getScore())).append(")");
            }
            log.info("📋 [RAG] 检索到的相关文件:{}", fileList);
        } else {
            log.info("⚠️ [RAG] 未检索到相关代码片段: appId={} (可能是首次对话或索引为空)", appId);
        }

        // 3. 添加用户需求
        enhanced.append("=== 用户需求 ===\n");
        enhanced.append(userMessage);

        return enhanced.toString();
    }

    /**
     * 格式化代码片段用于展示
     * 标注内容完整性：完整内容可直接用于 modifyFile，截断内容需要先 readFile
     */
    private String formatChunkForDisplay(ContextChunk chunk) {
        StringBuilder sb = new StringBuilder();

        // 提取文件基础路径（去掉 #template, #script 等后缀）
        String displayPath = chunk.getFilePath();
        String chunkSection = "";
        if (displayPath.contains("#")) {
            chunkSection = displayPath.substring(displayPath.indexOf("#") + 1);
            displayPath = displayPath.substring(0, displayPath.indexOf("#"));
        }

        // 添加代码内容
        String content = chunk.getContent();
        String language = getLanguageTag(chunk.getFilePath());

        // 判断是否需要截断
        boolean isTruncated = content.length() > MAX_CHUNK_DISPLAY_SIZE;
        boolean isPartialFile = StrUtil.isNotBlank(chunkSection);

        // 根据完整性添加不同标注
        if (isTruncated) {
            // 内容被截断：需要 readFile
            sb.append(String.format("📄 [%s] 相关度: %.2f\n", displayPath, chunk.getScore()));
            sb.append("⚠️ 内容已截断，如需修改此文件请先调用 readFile 获取完整内容\n");
            content = content.substring(0, MAX_CHUNK_DISPLAY_SIZE) + "\n// ... 内容已截断 ...";
        } else if (isPartialFile) {
            // Vue SFC 部分片段（template/script/style）：可用于 modifyFile，但需注意是部分内容
            sb.append(String.format("📄 [%s] <%s>部分 相关度: %.2f\n", displayPath, chunkSection, chunk.getScore()));
            sb.append("✅ 此为文件的 " + chunkSection + " 部分，内容完整，可直接用于 modifyFile\n");
        } else {
            // 完整内容：可直接用于 modifyFile
            sb.append(String.format("📄 [%s] 相关度: %.2f\n", displayPath, chunk.getScore()));
            sb.append("✅ 内容完整，可直接用于 modifyFile，无需调用 readFile\n");
        }

        sb.append("```").append(language).append("\n");
        sb.append(content);
        if (!content.endsWith("\n")) {
            sb.append("\n");
        }
        sb.append("```\n\n");

        return sb.toString();
    }

    /**
     * 根据文件路径获取代码块语言标识
     */
    private String getLanguageTag(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.contains(".vue")) return "vue";
        if (lowerPath.endsWith(".js")) return "javascript";
        if (lowerPath.endsWith(".ts")) return "typescript";
        if (lowerPath.endsWith(".css")) return "css";
        if (lowerPath.endsWith(".json")) return "json";
        if (lowerPath.endsWith(".html")) return "html";
        return "";
    }
}


