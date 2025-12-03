package com.frank.aicodehelper.service;

import com.frank.aicodehelper.model.enums.CodeGenTypeEnum;

/**
 * 项目状态摘要服务
 * 用于在每次用户请求时动态生成项目状态摘要，注入到 AI 请求中
 * 确保 AI 即使在对话历史被截断的情况下也能了解项目当前结构
 *
 * @author Frank
 */
public interface ProjectSummaryService {

    /**
     * 生成项目状态摘要
     *
     * @param appId       应用ID
     * @param codeGenType 代码生成类型
     * @return 项目状态摘要文本，如果项目不存在则返回空字符串
     */
    String generateProjectSummary(Long appId, CodeGenTypeEnum codeGenType);

    /**
     * 将用户消息与项目状态摘要合并
     * 如果项目存在，将摘要添加到用户消息前面
     *
     * @param userMessage 原始用户消息
     * @param appId       应用ID
     * @param codeGenType 代码生成类型
     * @return 增强后的用户消息
     */
    String enhanceUserMessage(String userMessage, Long appId, CodeGenTypeEnum codeGenType);
}

