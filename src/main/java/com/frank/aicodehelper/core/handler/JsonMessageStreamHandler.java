package com.frank.aicodehelper.core.handler;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.frank.aicodehelper.ai.model.message.AiResponseMessage;
import com.frank.aicodehelper.ai.model.message.StreamMessage;
import com.frank.aicodehelper.ai.model.message.ToolExecutedMessage;
import com.frank.aicodehelper.ai.model.message.ToolRequestMessage;
import com.frank.aicodehelper.ai.tools.BaseTool;
import com.frank.aicodehelper.ai.tools.ToolManager;
import com.frank.aicodehelper.model.entity.User;
import com.frank.aicodehelper.model.enums.StreamMessageTypeEnum;
import com.frank.aicodehelper.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

/**
 * JSON 消息流处理器
 * 处理 VUE_PROJECT 类型的复杂流式响应,包含工具调用信息
 * 
 * 【方案B】此 Handler 只负责格式转换和前端输出
 * 保存逻辑已移至 AiCodeGeneratorFacade.processTokenStream 的 onCompleteResponse
 * 这样即使用户刷新页面断开 SSE 连接，也能保存完整的对话记录
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private ToolManager toolManager;

    /**
     * 处理 TokenStream(VUE_PROJECT)
     * 解析 JSON 消息并重组为完整的响应格式
     * 
     * 【方案B】此方法只负责格式化输出到前端，不再负责保存到数据库
     * 保存逻辑已移至 AiCodeGeneratorFacade.processTokenStream 的 onCompleteResponse
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务（保留参数以兼容接口，实际不再使用）
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用（仅用于前端显示去重）
        Set<String> seenToolIds = new HashSet<>();
        
        return originFlux
                .map(chunk -> {
                    // 解析每个 JSON 消息块，格式化输出到前端
                    return handleJsonMessageChunk(chunk, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤空字串
                // 【方案B】移除 doOnComplete 中的保存逻辑
                // 保存已在 AiCodeGeneratorFacade.processTokenStream 的 onCompleteResponse 中完成
                .doOnError(error -> {
                    // 记录错误日志
                    log.error("流处理出错: {}", error.getMessage());
                });
    }

    /**
     * 解析 JSON 消息块（简化版，不再收集数据用于保存）
     * 仅负责格式化输出到前端
     */
    private String handleJsonMessageChunk(String chunk, Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                // 直接返回 AI 响应内容
                return aiMessage.getData();
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                String toolName = toolRequestMessage.getName();
                
                // 检查是否是第一次看到这个工具 ID（前端显示去重）
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    seenToolIds.add(toolId);
                    // 根据工具名称获取工具实例
                    BaseTool tool = toolManager.getTool(toolName);
                    // 返回格式化的工具调用信息
                    return tool.generateToolRequestResponse();
                } else {
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                // 根据工具名称获取工具实例
                String toolName = toolExecutedMessage.getName();
                BaseTool tool = toolManager.getTool(toolName);
                String result = tool.generateToolExecutedResult(jsonObject);
                // 返回格式化的输出
                return String.format("\n\n%s\n\n", result);
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }
}

