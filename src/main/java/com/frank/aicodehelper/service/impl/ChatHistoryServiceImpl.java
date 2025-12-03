package com.frank.aicodehelper.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.frank.aicodehelper.constant.UserConstant;
import com.frank.aicodehelper.exception.ErrorCode;
import com.frank.aicodehelper.exception.ThrowUtils;
import com.frank.aicodehelper.model.dto.chathistory.ChatHistoryQueryRequest;
import com.frank.aicodehelper.model.entity.App;
import com.frank.aicodehelper.model.entity.User;
import com.frank.aicodehelper.model.enums.ChatHistoryMessageTypeEnum;
import com.frank.aicodehelper.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.frank.aicodehelper.model.entity.ChatHistory;
import com.frank.aicodehelper.mapper.ChatHistoryMapper;
import com.frank.aicodehelper.service.ChatHistoryService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author 1
 * @since 2025-10-28
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 查询包装类
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                       LocalDateTime lastCreateTime,
                                                       User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, ChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    // 关键修改：正确重建 AiMessage（支持工具调用）
                    AiMessage aiMessage = rebuildAiMessage(history);
                    chatMemory.add(aiMessage);
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

    @Override
    public boolean addAiMessageWithToolCalls(Long appId, String message,
                                              List<ToolExecutionRequest> toolCalls,
                                              Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");

        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(StrUtil.isBlank(message) ? "" : message)
                .messageType(ChatHistoryMessageTypeEnum.AI.getValue())
                .userId(userId)
                .build();

        // 处理工具调用
        if (CollUtil.isNotEmpty(toolCalls)) {
            chatHistory.setHasToolCalls(true);
            // 序列化工具调用（只保存必要信息）
            chatHistory.setToolCalls(serializeToolCalls(toolCalls));
            log.debug("保存 AI 消息包含 {} 个工具调用", toolCalls.size());
        } else {
            chatHistory.setHasToolCalls(false);
        }

        return this.save(chatHistory);
    }

    /**
     * 重建 AiMessage（支持工具调用）
     */
    private AiMessage rebuildAiMessage(ChatHistory history) {
        String messageText = history.getMessage();

        // 如果有工具调用，重建带 tool_calls 的 AiMessage
        if (Boolean.TRUE.equals(history.getHasToolCalls()) && StrUtil.isNotBlank(history.getToolCalls())) {
            List<ToolExecutionRequest> toolCalls = deserializeToolCalls(history.getToolCalls());
            if (CollUtil.isNotEmpty(toolCalls)) {
                // 带工具调用的 AiMessage
                return AiMessage.from(messageText, toolCalls);
            }
        }

        // 普通文本消息
        return AiMessage.from(StrUtil.isBlank(messageText) ? "" : messageText);
    }

    /**
     * 序列化工具调用请求
     */
    private String serializeToolCalls(List<ToolExecutionRequest> toolCalls) {
        if (CollUtil.isEmpty(toolCalls)) {
            return null;
        }
        // 转换为简化的 JSON 结构
        List<ToolCallDTO> dtoList = toolCalls.stream()
                .map(tc -> new ToolCallDTO(tc.id(), tc.name(), tc.arguments()))
                .toList();
        return JSONUtil.toJsonStr(dtoList);
    }

    /**
     * 反序列化工具调用请求
     */
    private List<ToolExecutionRequest> deserializeToolCalls(String toolCallsJson) {
        if (StrUtil.isBlank(toolCallsJson)) {
            return null;
        }
        try {
            List<ToolCallDTO> dtoList = JSONUtil.toList(toolCallsJson, ToolCallDTO.class);
            return dtoList.stream()
                    .map(dto -> ToolExecutionRequest.builder()
                            .id(dto.getId())
                            .name(dto.getName())
                            .arguments(dto.getArguments())
                            .build())
                    .toList();
        } catch (Exception e) {
            log.warn("反序列化工具调用失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 工具调用 DTO（用于 JSON 序列化）
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ToolCallDTO {
        private String id;
        private String name;
        private String arguments;
    }

}

