package com.frank.aicodehelper.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 检索结果片段
 * 表示从向量数据库检索到的相关代码片段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextChunk {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 代码内容
     */
    private String content;

    /**
     * 片段类型
     */
    private String chunkType;

    /**
     * 相似度得分 (0-1)
     */
    private Double score;

    /**
     * 元数据
     */
    private Map<String, String> metadata;
}


