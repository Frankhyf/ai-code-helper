package com.frank.aicodehelper.rag.service;

import com.frank.aicodehelper.model.enums.CodeGenTypeEnum;

/**
 * RAG 消息增强服务接口
 * 负责将 RAG 检索到的相关代码上下文注入到用户消息中
 */
public interface RagEnhancedMessageService {

    /**
     * 增强用户消息
     * 将 RAG 检索到的相关代码和项目结构信息注入到用户消息中
     *
     * @param userMessage 原始用户消息
     * @param appId       应用ID
     * @param codeGenType 代码生成类型
     * @return 增强后的用户消息
     */
    String enhanceMessage(String userMessage, Long appId, CodeGenTypeEnum codeGenType);
}


