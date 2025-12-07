package com.frank.aicodehelper.rag.config;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里通义千问 Embedding 模型配置
 * 使用 text-embedding-v2 模型进行文本向量化
 * 向量存储使用内存存储（InMemoryEmbeddingStore）
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
     * 使用 InMemoryEmbeddingStore 替代 RedisEmbeddingStore
     * 优点：无需 RediSearch 模块，适合开发和小规模部署
     * 注意：应用重启后向量索引会丢失，但会在工具调用时自动重建
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("初始化内存向量存储 (InMemoryEmbeddingStore)");
        return new InMemoryEmbeddingStore<>();
    }
}


