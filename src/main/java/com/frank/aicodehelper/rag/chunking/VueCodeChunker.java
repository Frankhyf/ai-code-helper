package com.frank.aicodehelper.rag.chunking;

import cn.hutool.core.util.StrUtil;
import com.frank.aicodehelper.rag.config.RagConfig;
import com.frank.aicodehelper.rag.model.CodeContextDocument;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vue 项目代码分块器
 * 按文件为单位进行分块，保持语义完整性
 */
@Slf4j
@Component
public class VueCodeChunker implements ChunkingStrategy {

    @Resource
    private RagConfig ragConfig;

    /**
     * 匹配 Vue 组件名称
     */
    private static final Pattern COMPONENT_NAME_PATTERN =
            Pattern.compile("name:\\s*['\"]([^'\"]+)['\"]");

    /**
     * 支持的文件扩展名
     */
    private static final List<String> SUPPORTED_EXTENSIONS =
            List.of(".vue", ".js", ".ts", ".css", ".json", ".html");

    @Override
    public List<CodeContextDocument> chunk(Long appId, String filePath, String content) {
        if (StrUtil.isBlank(content)) {
            return Collections.emptyList();
        }

        List<CodeContextDocument> chunks = new ArrayList<>();
        int maxChunkSize = ragConfig.getIndexing().getMaxChunkSize();

        // 根据文件类型确定分块策略
        String fileType = getFileType(filePath);
        String chunkType = getChunkType(filePath);

        if (content.length() <= maxChunkSize) {
            // 整文件作为一个 chunk
            chunks.add(createChunk(appId, filePath, content, fileType, chunkType, 0));
        } else {
            // 大文件分割策略
            if (filePath.endsWith(".vue")) {
                chunks.addAll(splitVueFile(appId, filePath, content, fileType));
            } else {
                chunks.addAll(splitBySize(appId, filePath, content, fileType, chunkType, maxChunkSize));
            }
        }

        log.debug("文件 {} 分块完成，共 {} 个片段", filePath, chunks.size());
        return chunks;
    }

    /**
     * Vue 文件按 template/script/style 分割
     */
    private List<CodeContextDocument> splitVueFile(Long appId, String filePath,
                                                    String content, String fileType) {
        List<CodeContextDocument> chunks = new ArrayList<>();
        int index = 0;

        // 提取 <template> 部分
        String template = extractBlock(content, "<template>", "</template>");
        if (StrUtil.isNotBlank(template)) {
            chunks.add(createChunk(appId, filePath + "#template",
                    "<template>" + template + "</template>",
                    fileType, "TEMPLATE", index++));
        }

        // 提取 <script> 部分（支持 <script setup>）
        String script = extractScriptBlock(content);
        if (StrUtil.isNotBlank(script)) {
            chunks.add(createChunk(appId, filePath + "#script",
                    script, fileType, "SCRIPT", index++));
        }

        // 提取 <style> 部分
        String style = extractBlock(content, "<style", "</style>");
        if (StrUtil.isNotBlank(style)) {
            // 找到完整的 style 标签
            int styleStart = content.indexOf("<style");
            int styleEnd = content.indexOf("</style>") + "</style>".length();
            if (styleStart >= 0 && styleEnd > styleStart) {
                String fullStyle = content.substring(styleStart, styleEnd);
                chunks.add(createChunk(appId, filePath + "#style",
                        fullStyle, fileType, "STYLE", index++));
            }
        }

        // 如果分割后没有任何内容，则保留整个文件
        if (chunks.isEmpty()) {
            chunks.add(createChunk(appId, filePath, content, fileType, "COMPONENT", 0));
        }

        return chunks;
    }

    /**
     * 按大小分割文件
     */
    private List<CodeContextDocument> splitBySize(Long appId, String filePath,
                                                   String content, String fileType,
                                                   String chunkType, int maxSize) {
        List<CodeContextDocument> chunks = new ArrayList<>();
        int index = 0;

        // 按行分割，尽量在行边界切分
        String[] lines = content.split("\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String line : lines) {
            if (currentChunk.length() + line.length() + 1 > maxSize && currentChunk.length() > 0) {
                // 保存当前 chunk
                chunks.add(createChunk(appId, filePath + "#part" + index,
                        currentChunk.toString(), fileType, chunkType, index));
                currentChunk = new StringBuilder();
                index++;
            }
            if (currentChunk.length() > 0) {
                currentChunk.append("\n");
            }
            currentChunk.append(line);
        }

        // 保存最后一个 chunk
        if (currentChunk.length() > 0) {
            chunks.add(createChunk(appId, filePath + "#part" + index,
                    currentChunk.toString(), fileType, chunkType, index));
        }

        return chunks;
    }

    /**
     * 提取代码块内容
     */
    private String extractBlock(String content, String startTag, String endTag) {
        int start = content.indexOf(startTag);
        int end = content.indexOf(endTag);
        if (start >= 0 && end > start) {
            start += startTag.length();
            return content.substring(start, end).trim();
        }
        return null;
    }

    /**
     * 提取 script 块（支持 <script setup> 和普通 <script>）
     */
    private String extractScriptBlock(String content) {
        // 匹配 <script> 或 <script setup> 或 <script lang="ts"> 等
        Pattern scriptPattern = Pattern.compile("<script[^>]*>([\\s\\S]*?)</script>",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = scriptPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(0);  // 返回完整的 script 标签
        }
        return null;
    }

    /**
     * 创建 CodeContextDocument
     */
    private CodeContextDocument createChunk(Long appId, String filePath, String content,
                                            String fileType, String chunkType, int index) {
        Map<String, Object> metadata = extractMetadata(filePath, content, fileType);
        LocalDateTime now = LocalDateTime.now();

        return CodeContextDocument.builder()
                .appId(appId)
                .chunkId(UUID.randomUUID().toString())
                .filePath(filePath)
                .content(content)
                .fileType(fileType)
                .chunkType(chunkType)
                .metadata(metadata)
                .chunkIndex(index)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    /**
     * 提取元数据
     */
    private Map<String, Object> extractMetadata(String filePath, String content, String fileType) {
        Map<String, Object> metadata = new HashMap<>();

        // 提取文件名
        String fileName = filePath.contains("/")
                ? filePath.substring(filePath.lastIndexOf('/') + 1)
                : filePath;
        metadata.put("fileName", fileName);

        // Vue 文件特有元数据
        if ("vue".equals(fileType)) {
            // 提取组件名
            Matcher nameMatcher = COMPONENT_NAME_PATTERN.matcher(content);
            String componentName = nameMatcher.find()
                    ? nameMatcher.group(1)
                    : fileName.replace(".vue", "");
            metadata.put("componentName", componentName);

            // 检查各部分是否存在
            metadata.put("hasTemplate", content.contains("<template>"));
            metadata.put("hasScript", content.contains("<script"));
            metadata.put("hasStyle", content.contains("<style"));
            metadata.put("isScriptSetup", content.contains("<script setup"));
        }

        return metadata;
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".vue")) return "vue";
        if (lowerPath.endsWith(".js")) return "javascript";
        if (lowerPath.endsWith(".ts")) return "typescript";
        if (lowerPath.endsWith(".css")) return "css";
        if (lowerPath.endsWith(".json")) return "json";
        if (lowerPath.endsWith(".html")) return "html";
        return "unknown";
    }

    /**
     * 获取片段类型
     */
    private String getChunkType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".vue")) return "COMPONENT";
        if (lowerPath.endsWith(".js") || lowerPath.endsWith(".ts")) return "MODULE";
        if (lowerPath.endsWith(".css")) return "STYLE";
        if (lowerPath.endsWith(".json")) return "CONFIG";
        if (lowerPath.endsWith(".html")) return "HTML";
        return "FILE";
    }

    @Override
    public List<String> getSupportedExtensions() {
        return SUPPORTED_EXTENSIONS;
    }
}


