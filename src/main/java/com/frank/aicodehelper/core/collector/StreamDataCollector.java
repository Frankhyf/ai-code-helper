package com.frank.aicodehelper.core.collector;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 流式数据收集器
 * 用于在 TokenStream 回调中收集 AI 响应内容和工具调用信息
 * 确保即使前端断开连接，也能保存完整的对话记录
 */
@Getter
@Slf4j
public class StreamDataCollector {
    
    private final StringBuilder responseBuilder = new StringBuilder();
    private final List<ToolExecutionRequest> toolCalls = new ArrayList<>();
    private final Set<String> seenToolIds = new HashSet<>();
    
    /**
     * 追加 AI 响应内容
     */
    public synchronized void appendResponse(String content) {
        if (content != null) {
            responseBuilder.append(content);
        }
    }
    
    /**
     * 添加工具调用（自动去重）
     */
    public synchronized boolean addToolCall(ToolExecutionRequest toolCall) {
        if (toolCall != null && toolCall.id() != null 
            && !seenToolIds.contains(toolCall.id())) {
            seenToolIds.add(toolCall.id());
            toolCalls.add(toolCall);
            return true;  // 新增成功
        }
        return false;  // 已存在或无效
    }
    
    /**
     * 检查工具ID是否已存在
     */
    public boolean hasSeenToolId(String toolId) {
        return toolId != null && seenToolIds.contains(toolId);
    }
    
    /**
     * 获取收集的完整响应
     */
    public String getResponse() {
        return responseBuilder.toString();
    }
    
    /**
     * 是否有工具调用
     */
    public boolean hasToolCalls() {
        return !toolCalls.isEmpty();
    }
    
    /**
     * 获取工具调用数量
     */
    public int getToolCallCount() {
        return toolCalls.size();
    }
}
