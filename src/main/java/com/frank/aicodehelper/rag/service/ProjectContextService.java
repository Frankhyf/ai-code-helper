package com.frank.aicodehelper.rag.service;

import com.frank.aicodehelper.rag.model.ContextChunk;

import java.util.List;

/**
 * 项目上下文服务接口
 * 负责代码的向量化索引和语义检索
 */
public interface ProjectContextService {

    /**
     * 索引代码文件
     *
     * @param appId    应用ID
     * @param filePath 文件相对路径
     * @param content  文件内容
     */
    void indexCodeFile(Long appId, String filePath, String content);

    /**
     * 搜索相关代码上下文
     *
     * @param appId    应用ID
     * @param query    查询文本
     * @param topK     返回数量
     * @param minScore 最低相似度阈值
     * @return 相关代码片段列表
     */
    List<ContextChunk> searchContext(Long appId, String query, int topK, double minScore);

    /**
     * 使用默认参数搜索相关代码上下文
     *
     * @param appId 应用ID
     * @param query 查询文本
     * @return 相关代码片段列表
     */
    List<ContextChunk> searchContext(Long appId, String query);

    /**
     * 删除指定应用的所有索引
     *
     * @param appId 应用ID
     */
    void deleteByAppId(Long appId);

    /**
     * 删除指定文件的索引
     *
     * @param appId    应用ID
     * @param filePath 文件路径
     */
    void deleteByFilePath(Long appId, String filePath);

    /**
     * 检查 RAG 是否启用
     *
     * @return 是否启用
     */
    boolean isEnabled();
}


