package com.frank.aicodehelper.rag.config;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里通义千问 Embedding 模型配置
 * 使用 text-embedding-v2 模型进行文本向量化
 * 向量存储使用 Redis Stack / RediSearch（持久化）
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "rag.embedding")
public class DashScopeEmbeddingConfig {

    /**
     * DashScope API Key
     */
    private String apiKey;

    /**
     * 模型名称，默认使用 text-embedding-v2
     */
    private String modelName = "text-embedding-v2";

    /**
     * 向量维度（text-embedding-v2 使用 1536 维）
     */
    private int dimension = 1536;

    /**
     * Redis Vector Store 配置（需要 RediSearch 支持）
     */
    private RedisConfig redis = new RedisConfig();

    @Data
    public static class RedisConfig {
        private String host = "localhost";
        /**
         * 如使用 docker 映射 6380:6379，需将此处改为 6380
         */
        private int port = 6380;
        private String indexName = "code_context_idx";
        private String prefix = "code:context:";
        /**
         * 可选：COSINE / IP / L2
         */
        private String distanceType = "COSINE";
    }

    /**
     * 创建阿里通义千问 Embedding 模型 Bean
     */
    @Bean
    public EmbeddingModel dashScopeEmbeddingModel() {
        log.info("初始化阿里通义千问 Embedding 模型: {}", modelName);
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    /**
     * 创建内存向量存储 Bean
     * 使用 RedisEmbeddingStore（依赖 RediSearch）
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("初始化 Redis 向量存储: {}:{}, 索引名: {}, 维度: {}, 距离度量: {}",
                redis.getHost(), redis.getPort(), redis.getIndexName(), dimension, redis.getDistanceType());

        return RedisEmbeddingStore.builder()
                .host(redis.getHost())
                .port(redis.getPort())
                .indexName(redis.getIndexName())
                .prefix(redis.getPrefix())
                .dimension(dimension)
                .build();
    }
}

