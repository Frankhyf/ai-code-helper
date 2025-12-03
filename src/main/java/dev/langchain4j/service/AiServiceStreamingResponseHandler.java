package dev.langchain4j.service;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.guardrail.ChatExecutor;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * Handles response from a language model for AI Service that is streamed token-by-token. Handles both regular (text)
 * responses and responses with the request to execute one or multiple tools.
 */
@Internal
class AiServiceStreamingResponseHandler implements StreamingChatResponseHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AiServiceStreamingResponseHandler.class);

    /**
     * 最大连续工具调用次数限制
     * 防止 AI 无限循环调用工具
     */
    private static final int MAX_SEQUENTIAL_TOOL_INVOCATIONS = 20;  // 正式限制值

    static {
        // 启动时打印，确认自定义类被加载
        LOG.info("========================================");
        LOG.info("✅ 自定义 AiServiceStreamingResponseHandler 已加载");
        LOG.info("工具调用次数限制: {}", MAX_SEQUENTIAL_TOOL_INVOCATIONS);
        LOG.info("========================================");
    }

    private final ChatExecutor chatExecutor;
    private final AiServiceContext context;
    private final Object memoryId;
    private final GuardrailRequestParams commonGuardrailParams;
    private final Object methodKey;

    private final Consumer<String> partialResponseHandler;
    private final BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler;
    private final BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler;
    private final Consumer<ToolExecution> toolExecutionHandler;
    private final Consumer<ChatResponse> completeResponseHandler;

    private final Consumer<Throwable> errorHandler;

    private final ChatMemory temporaryMemory;
    private final TokenUsage tokenUsage;

    private final List<ToolSpecification> toolSpecifications;
    private final Map<String, ToolExecutor> toolExecutors;
    private final List<String> responseBuffer = new ArrayList<>();
    private final boolean hasOutputGuardrails;

    /**
     * 当前已执行的工具调用次数（用于递归传递）
     */
    private final int currentToolInvocationCount;

    AiServiceStreamingResponseHandler(
            ChatExecutor chatExecutor,
            AiServiceContext context,
            Object memoryId,
            Consumer<String> partialResponseHandler,
            BiConsumer<Integer, ToolExecutionRequest> partialToolExecutionRequestHandler,
            BiConsumer<Integer, ToolExecutionRequest> completeToolExecutionRequestHandler,
            Consumer<ToolExecution> toolExecutionHandler,
            Consumer<ChatResponse> completeResponseHandler,
            Consumer<Throwable> errorHandler,
            ChatMemory temporaryMemory,
            TokenUsage tokenUsage,
            List<ToolSpecification> toolSpecifications,
            Map<String, ToolExecutor> toolExecutors,
            GuardrailRequestParams commonGuardrailParams,
            Object methodKey,
            int currentToolInvocationCount) {
        this.chatExecutor = ensureNotNull(chatExecutor, "chatExecutor");
        this.context = ensureNotNull(context, "context");
        this.memoryId = ensureNotNull(memoryId, "memoryId");
        this.methodKey = methodKey;

        this.partialResponseHandler = ensureNotNull(partialResponseHandler, "partialResponseHandler");
        this.partialToolExecutionRequestHandler = partialToolExecutionRequestHandler;
        this.completeToolExecutionRequestHandler = completeToolExecutionRequestHandler;
        this.completeResponseHandler = completeResponseHandler;
        this.toolExecutionHandler = toolExecutionHandler;
        this.errorHandler = errorHandler;

        this.temporaryMemory = temporaryMemory;
        this.tokenUsage = ensureNotNull(tokenUsage, "tokenUsage");
        this.commonGuardrailParams = commonGuardrailParams;

        this.toolSpecifications = copy(toolSpecifications);
        this.toolExecutors = copy(toolExecutors);
        this.hasOutputGuardrails = context.guardrailService().hasOutputGuardrails(methodKey);
        this.currentToolInvocationCount = currentToolInvocationCount;
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        // If we're using output guardrails, then buffer the partial response until the guardrails have completed
        if (hasOutputGuardrails) {
            responseBuffer.add(partialResponse);
        } else {
            partialResponseHandler.accept(partialResponse);
        }
    }

    @Override
    public void onPartialToolExecutionRequest(int index, ToolExecutionRequest partialToolExecutionRequest) {
        // If we're using output guardrails, then buffer the partial response until the guardrails have completed
        partialToolExecutionRequestHandler.accept(index, partialToolExecutionRequest);
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        // 防御性检查：超时或连接中断可能导致 completeResponse 为 null
        if (completeResponse == null) {
            LOG.warn("收到 null 的 completeResponse，可能是由于超时或连接中断");
            if (errorHandler != null) {
                errorHandler.accept(new RuntimeException("AI 响应不完整，可能超时或连接中断"));
            }
            return;
        }
        AiMessage aiMessage = completeResponse.aiMessage();
        addToMemory(aiMessage);

        if (aiMessage.hasToolExecutionRequests()) {
            // 计算本次调用后的累计工具调用次数
            int toolRequestCount = aiMessage.toolExecutionRequests().size();
            int newToolInvocationCount = currentToolInvocationCount + toolRequestCount;

            // 检查是否超过最大工具调用次数限制
            if (newToolInvocationCount > MAX_SEQUENTIAL_TOOL_INVOCATIONS) {
                LOG.warn("🚫🚫🚫 工具调用次数超过限制！！！");
                LOG.warn("当前调用次数: {}, 限制: {}", newToolInvocationCount, MAX_SEQUENTIAL_TOOL_INVOCATIONS);
                LOG.warn("强制终止工具调用循环，返回终止消息给用户");
                // 超过限制，直接完成响应，不再继续调用工具
                if (completeResponseHandler != null) {
                    ChatResponse finalChatResponse = ChatResponse.builder()
                            .aiMessage(AiMessage.from("工具调用次数已达到上限（" + MAX_SEQUENTIAL_TOOL_INVOCATIONS + " 次），请重新发起对话继续操作。"))
                            .metadata(completeResponse.metadata().toBuilder()
                                    .tokenUsage(tokenUsage.add(completeResponse.metadata().tokenUsage()))
                                    .build())
                            .build();
                    completeResponseHandler.accept(finalChatResponse);
                }
                return;
            }

            for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
                String toolName = toolExecutionRequest.name();
                ToolExecutor toolExecutor = toolExecutors.get(toolName);
                String toolExecutionResult = toolExecutor.execute(toolExecutionRequest, memoryId);
                ToolExecutionResultMessage toolExecutionResultMessage =
                        ToolExecutionResultMessage.from(toolExecutionRequest, toolExecutionResult);
                addToMemory(toolExecutionResultMessage);

                if (toolExecutionHandler != null) {
                    ToolExecution toolExecution = ToolExecution.builder()
                            .request(toolExecutionRequest)
                            .result(toolExecutionResult)
                            .build();
                    toolExecutionHandler.accept(toolExecution);
                }
            }

            // 改为 INFO 级别，方便跟踪工具调用进度
            LOG.info("🔧 工具调用进度: {}/{}", newToolInvocationCount, MAX_SEQUENTIAL_TOOL_INVOCATIONS);

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messagesToSend(memoryId))
                    .toolSpecifications(toolSpecifications)
                    .build();

            var handler = new AiServiceStreamingResponseHandler(
                    chatExecutor,
                    context,
                    memoryId,
                    partialResponseHandler,
                    partialToolExecutionRequestHandler,
                    completeToolExecutionRequestHandler,
                    toolExecutionHandler,
                    completeResponseHandler,
                    errorHandler,
                    temporaryMemory,
                    TokenUsage.sum(tokenUsage, completeResponse.metadata().tokenUsage()),
                    toolSpecifications,
                    toolExecutors,
                    commonGuardrailParams,
                    methodKey,
                    newToolInvocationCount);  // 传递累计的工具调用次数

            context.streamingChatModel.chat(chatRequest, handler);
        } else {
            if (completeResponseHandler != null) {
                ChatResponse finalChatResponse = ChatResponse.builder()
                        .aiMessage(aiMessage)
                        .metadata(completeResponse.metadata().toBuilder()
                                .tokenUsage(tokenUsage.add(
                                        completeResponse.metadata().tokenUsage()))
                                .build())
                        .build();

                // Invoke output guardrails
                if (hasOutputGuardrails) {
                    if (commonGuardrailParams != null) {
                        var newCommonParams = GuardrailRequestParams.builder()
                                .chatMemory(getMemory())
                                .augmentationResult(commonGuardrailParams.augmentationResult())
                                .userMessageTemplate(commonGuardrailParams.userMessageTemplate())
                                .variables(commonGuardrailParams.variables())
                                .build();

                        var outputGuardrailParams = OutputGuardrailRequest.builder()
                                .responseFromLLM(finalChatResponse)
                                .chatExecutor(chatExecutor)
                                .requestParams(newCommonParams)
                                .build();

                        finalChatResponse =
                                context.guardrailService().executeGuardrails(methodKey, outputGuardrailParams);
                    }

                    // If we have output guardrails, we should process all of the partial responses first before
                    // completing
                    responseBuffer.forEach(partialResponseHandler::accept);
                    responseBuffer.clear();
                }

                // TODO should completeResponseHandler accept all ChatResponses that happened?
                completeResponseHandler.accept(finalChatResponse);
            }
        }
    }

    private ChatMemory getMemory() {
        return getMemory(memoryId);
    }

    private ChatMemory getMemory(Object memId) {
        return context.hasChatMemory() ? context.chatMemoryService.getOrCreateChatMemory(memoryId) : temporaryMemory;
    }

    private void addToMemory(ChatMessage chatMessage) {
        getMemory().add(chatMessage);
    }

    private List<ChatMessage> messagesToSend(Object memoryId) {
        return getMemory(memoryId).messages();
    }

    @Override
    public void onError(Throwable error) {
        if (errorHandler != null) {
            try {
                errorHandler.accept(error);
            } catch (Exception e) {
                LOG.error("While handling the following error...", error);
                LOG.error("...the following error happened", e);
            }
        } else {
            LOG.warn("Ignored error", error);
        }
    }
}
