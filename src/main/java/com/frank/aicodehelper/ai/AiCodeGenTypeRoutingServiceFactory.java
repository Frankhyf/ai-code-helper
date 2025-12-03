package com.frank.aicodehelper.ai;

import com.frank.aicodehelper.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * AI代码生成类型路由服务工厂
 *
 * @author Frank
 */
@Slf4j
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 创建AI代码生成类型路由服务实例
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        // 动态获取多例的路由 ChatModel，支持并发
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }

    /**
     * 默认提供一个 Bean
     * 使用 @Lazy 延迟初始化，避免在 SpringContextUtil 初始化前被调用
     */
    @Bean
    @Lazy
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }
}
