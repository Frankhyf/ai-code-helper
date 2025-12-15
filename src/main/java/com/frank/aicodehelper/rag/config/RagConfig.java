package com.frank.aicodehelper.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 配置类
 * 从配置文件读取 RAG 相关参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rag")
public class RagConfig {

    /**
     * 是否启用 RAG 功能
     */
    private boolean enabled = true;

    /**
     * 检索配置
     */
    private RetrievalConfig retrieval = new RetrievalConfig();

    /**
     * 索引配置
     */
    private IndexingConfig indexing = new IndexingConfig();

    @Data
    public static class RetrievalConfig {
        /**
         * 默认检索数量（固定返回 1 个，有 readFile 工具兜底）
         */
        private int defaultTopK = 1;

        /**
         * 默认最低相似度阈值（设为 0 保证始终返回 top1）
         */
        private double defaultMinScore = 0.0;

        /**
         * 是否保证至少返回 top1（即使低于阈值）
         * 注：当 minScore=0 时此配置不再需要，保留用于配置灵活性
         */
        private boolean guaranteeTopOne = true;
    }

    @Data
    public static class IndexingConfig {
        /**
         * 是否异步索引
         */
        private boolean async = true;

        /**
         * 单个 chunk 的最大字符数
         */
        private int maxChunkSize = 8000;

        /**
         * 分片重叠窗口大小（字符数）
         * 避免语义在分片边界断裂
         */
        private int chunkOverlap = 150;
    }
}


