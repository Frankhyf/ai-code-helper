package com.frank.aicodehelper.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 代码上下文文档（存入向量数据库）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeContextDocument {

    /**
     * 应用ID（用于过滤检索范围）
     */
    private Long appId;

    /**
     * 片段唯一ID
     */
    private String chunkId;

    /**
     * 文件相对路径
     * 如: src/components/Header.vue
     */
    private String filePath;

    /**
     * 代码内容（用于生成embedding和返回给AI）
     */
    private String content;

    /**
     * 文件类型: vue/js/ts/css/json
     */
    private String fileType;

    /**
     * 片段类型: COMPONENT/MODULE/STYLE/CONFIG
     */
    private String chunkType;

    /**
     * 扩展元数据
     * Vue组件: componentName, hasTemplate, hasScript, hasStyle
     * JS模块: exports, imports
     */
    private Map<String, Object> metadata;

    /**
     * 片段索引（同一文件多个chunk时的顺序）
     */
    private Integer chunkIndex;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}


