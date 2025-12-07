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
            enhanced.append("以下是与您需求语义相关的代码片段，可直接参考进行修改：\n\n");

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
     */
    private String formatChunkForDisplay(ContextChunk chunk) {
        StringBuilder sb = new StringBuilder();

        // 提取文件基础路径（去掉 #template, #script 等后缀）
        String displayPath = chunk.getFilePath();
        if (displayPath.contains("#")) {
            displayPath = displayPath.substring(0, displayPath.indexOf("#"));
        }

        // 添加文件路径和相关度
        sb.append(String.format("[%s] 相关度: %.2f\n", displayPath, chunk.getScore()));

        // 添加代码内容
        String content = chunk.getContent();
        String language = getLanguageTag(chunk.getFilePath());

        // 截断过长的内容
        if (content.length() > MAX_CHUNK_DISPLAY_SIZE) {
            content = content.substring(0, MAX_CHUNK_DISPLAY_SIZE) + "\n// ... 内容已截断 ...";
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


