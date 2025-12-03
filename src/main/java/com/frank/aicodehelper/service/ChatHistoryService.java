package com.frank.aicodehelper.service;

import com.frank.aicodehelper.model.dto.chathistory.ChatHistoryQueryRequest;
import com.frank.aicodehelper.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.frank.aicodehelper.model.entity.ChatHistory;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.memory.ChatMemory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author Frank
 * @since 2025-10-28
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话消息
     *
     * @param appId       应用ID
     * @param message     消息内容
     * @param messageType 消息类型
     * @param userId      用户ID
     * @return 是否成功
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 根据应用ID删除对话历史
     *
     * @param appId 应用ID
     * @return 是否成功
     */
    boolean deleteByAppId(Long appId);

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询包装类
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param loginUser      登录用户
     * @return 对话历史分页
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                LocalDateTime lastCreateTime,
                                                User loginUser);

    /**
     * 加载对话历史到记忆中
     *
     * @param appId       应用ID
     * @param chatMemory  对话记忆
     * @param maxCount    最大加载数量
     * @return 实际加载的数量
     */
    int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount);

    /**
     * 保存带工具调用的 AI 消息
     *
     * @param appId     应用ID
     * @param message   消息文本内容
     * @param toolCalls 工具调用请求列表
     * @param userId    用户ID
     * @return 是否成功
     */
    boolean addAiMessageWithToolCalls(Long appId, String message, 
                                       List<ToolExecutionRequest> toolCalls, 
                                       Long userId);

}
