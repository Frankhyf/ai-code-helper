package com.frank.aicodehelper.core.handler;

import com.frank.aicodehelper.model.entity.User;
import com.frank.aicodehelper.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * 简单文本流处理器
 * 处理 HTML 和 MULTI_FILE 类型的流式响应
 *
 * 【方案B】此 Handler 只负责透传流数据到前端
 * 保存逻辑已移至 AiCodeGeneratorFacade.processCodeStream 的 sink 回调中
 * 这样即使用户刷新页面断开 SSE 连接，也能保存完整的对话记录
 */
@Slf4j
public class SimpleTextStreamHandler {

    /**
     * 处理传统流(HTML, MULTI_FILE)
     * 
     * 【方案B】此方法只负责透传流数据，不再负责保存到数据库
     * 保存逻辑已移至 AiCodeGeneratorFacade.processCodeStream 的 sink 回调中
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务（保留参数以兼容接口，实际不再使用）
     * @param appId              应用ID
     * @param loginUser          登录用户
     * @return 处理后的流（直接透传）
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 【方案B】直接透传流，不再收集和保存
        // 保存逻辑已在 AiCodeGeneratorFacade.processCodeStream 中通过 sink 回调实现
        return originFlux
                .doOnError(error -> {
                    // 仅记录错误日志，不保存到数据库（保存已在上游处理）
                    log.error("App {} 流处理出错: {}", appId, error.getMessage());
                });
    }
}

