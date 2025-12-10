package com.frank.aicodehelper.core;

import cn.hutool.json.JSONUtil;
import com.frank.aicodehelper.ai.AiCodeGeneratorService;
import com.frank.aicodehelper.ai.AiCodeGeneratorServiceFactory;
import com.frank.aicodehelper.ai.model.HtmlCodeResult;
import com.frank.aicodehelper.ai.model.MultiFileCodeResult;
import com.frank.aicodehelper.ai.model.message.AiResponseMessage;
import com.frank.aicodehelper.ai.model.message.ToolExecutedMessage;
import com.frank.aicodehelper.ai.model.message.ToolRequestMessage;
import com.frank.aicodehelper.constant.AppConstant;
import com.frank.aicodehelper.core.builder.VueProjectBuilder;
import com.frank.aicodehelper.core.parser.CodeParserExecutor;
import com.frank.aicodehelper.core.saver.CodeFileSaverExecutor;
import com.frank.aicodehelper.core.collector.StreamDataCollector;
import com.frank.aicodehelper.exception.BusinessException;
import com.frank.aicodehelper.exception.ErrorCode;
import com.frank.aicodehelper.model.enums.CodeGenTypeEnum;
import com.frank.aicodehelper.service.ChatHistoryService;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI 代码生成门面类，组合代码生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    /**
     * 读取类工具列表（不向前端返回执行结果）
     * 这些工具的执行结果通常是大量文件内容，不适合直接展示给前端
     */
    private static final Set<String> SILENT_TOOLS = Set.of("readFile", "readDir", "readDirectory");

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private com.frank.aicodehelper.ai.tools.ToolManager toolManager;
    @Resource
    private com.frank.aicodehelper.rag.listener.ToolExecutionRagListener ragListener;

    /**
     * 统一入口：根据类型生成并保存代码（使用 appId）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据 appId 获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式，使用 appId）
     * 新增 userId 参数用于保存对话记录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,
                                                  Long appId, Long userId) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        // 根据 appId 和代码生成类型获取对应的 AI 服务实例
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId, userId);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId, userId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId, userId);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     * 【方案B核心】保存逻辑在 onCompleteResponse 中执行，确保即使用户刷新也能保存完整记录
     *
     * @param tokenStream TokenStream 对象
     * @param appId       应用 ID
     * @param userId      用户 ID
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId, Long userId) {
        // 创建数据收集器，在 TokenStream 回调中收集数据
        StreamDataCollector collector = new StreamDataCollector();
        // 使用 AtomicBoolean 确保只保存一次，避免重复保存
        AtomicBoolean saved = new AtomicBoolean(false);
        
        return Flux.<String>create(sink -> {
            // 关键：注册取消回调，当用户刷新导致连接断开时保存数据
            sink.onCancel(() -> {
                log.info("App {} 检测到连接取消，尝试保存已收集内容", appId);
                if (saved.compareAndSet(false, true)) {
                    saveCompleteResponse(appId, userId, collector);
                }
            });
            
            // 关键：注册销毁回调（双重保险）
            sink.onDispose(() -> {
                log.info("App {} Sink 被销毁，尝试保存已收集内容", appId);
                if (saved.compareAndSet(false, true)) {
                    saveCompleteResponse(appId, userId, collector);
                }
            });
            
            tokenStream.onPartialResponse((String partialResponse) -> {
                        // 收集 AI 文本响应
                        collector.appendResponse(partialResponse);
                        // 包装 sink.next 调用，避免异常传播导致回调链中断
                        try {
                            sink.next(JSONUtil.toJsonStr(new AiResponseMessage(partialResponse)));
                        } catch (Exception e) {
                            log.debug("发送响应失败（连接可能已断开）: {}", e.getMessage());
                        }
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        // 收集工具调用请求（自动去重）
                        collector.addToolCall(toolExecutionRequest);
                        try {
                            sink.next(JSONUtil.toJsonStr(new ToolRequestMessage(toolExecutionRequest)));
                        } catch (Exception e) {
                            log.debug("发送工具请求失败（连接可能已断开）: {}", e.getMessage());
                        }
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        // 收集格式化的工具执行结果（用于保存到数据库）
                        String toolName = toolExecution.request().name();
                        String formattedResult;
                        
                        if (SILENT_TOOLS.contains(toolName)) {
                            // 读取类工具：简单提示
                            formattedResult = "✅ 读取成功";
                        } else {
                            // 其他工具：使用格式化方法生成友好的显示内容
                            var tool = toolManager.getTool(toolName);
                            if (tool != null) {
                                cn.hutool.json.JSONObject args = JSONUtil.parseObj(toolExecution.request().arguments());
                                formattedResult = tool.generateToolExecutedResult(args);
                            } else {
                                // 工具未注册时使用原始结果
                                formattedResult = toolExecution.result();
                            }
                        }
                        collector.appendResponse("\n\n" + formattedResult + "\n\n");
                        
                        // 🆕 RAG 索引：工具执行后异步触发向量索引更新
                        ragListener.onToolExecuted(toolExecution, appId);
                        
                        try {
                            ToolExecutedMessage msg = new ToolExecutedMessage(toolExecution);
                            if (SILENT_TOOLS.contains(toolName)) {
                                msg.setResult(formattedResult);
                            }
                            sink.next(JSONUtil.toJsonStr(msg));
                        } catch (Exception e) {
                            log.debug("发送工具执行结果失败（连接可能已断开）: {}", e.getMessage());
                        }
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        log.info("App {} AI 响应完成", appId);
                        // 使用 CAS 确保只保存一次
                        if (saved.compareAndSet(false, true)) {
                            saveCompleteResponse(appId, userId, collector);
                        }
                        
                        // 使用虚拟线程异步执行 Vue 项目构建，避免阻塞流式响应
                        // 前端收到 complete 后可立即展示结果，构建在后台进行
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
                        Thread.startVirtualThread(() -> {
                            log.info("App {} 开始异步构建 Vue 项目: {}", appId, projectPath);
                            boolean success = vueProjectBuilder.buildProject(projectPath);
                            if (success) {
                                log.info("App {} Vue 项目构建成功", appId);
                            } else {
                                log.warn("App {} Vue 项目构建失败", appId);
                            }
                        });
                        
                        try {
                            sink.complete();
                        } catch (Exception e) {
                            log.debug("完成流失败（连接可能已断开）: {}", e.getMessage());
                        }
                    })
                    .onError((Throwable error) -> {
                        log.error("App {} AI 生成出错", appId, error);
                        // 使用 CAS 确保只保存一次
                        if (saved.compareAndSet(false, true)) {
                            saveCompleteResponse(appId, userId, collector);
                        }
                        
                        try {
                            sink.error(error);
                        } catch (Exception e) {
                            log.debug("发送错误失败（连接可能已断开）: {}", e.getMessage());
                        }
                    })
                    .start();
        });
    }
    
    /**
     * 保存完整的 AI 响应到数据库
     * 
     * @param appId     应用ID
     * @param userId    用户ID
     * @param collector 数据收集器
     */
    private void saveCompleteResponse(Long appId, Long userId, StreamDataCollector collector) {
        try {
            String response = collector.getResponse();
            if (response == null || response.isBlank()) {
                log.warn("App {} AI 响应为空，跳过保存", appId);
                return;
            }
            
            if (collector.hasToolCalls()) {
                chatHistoryService.addAiMessageWithToolCalls(
                    appId, response, collector.getToolCalls(), userId);
                log.info("App {} 保存完整 AI 消息，包含 {} 个工具调用", 
                    appId, collector.getToolCallCount());
            } else {
                chatHistoryService.addChatMessage(appId, response, "ai", userId);
                log.info("App {} 保存完整 AI 消息（无工具调用）", appId);
            }
        } catch (Exception e) {
            log.error("App {} 保存 AI 响应失败: {}", appId, e.getMessage(), e);
        }
    }

    /**
     * 通用流式代码处理方法（使用 appId）
     * 【方案B改进】保存逻辑在原始 AI 流完成后执行，而不是在前端断开时立即保存
     * 这样即使用户刷新页面，AI 后台继续生成，最终也能保存完整内容
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @param appId       应用 ID
     * @param userId      用户 ID
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType, 
                                           Long appId, Long userId) {
        // 🔑 关键：收集完整响应内容
        StringBuilder responseBuilder = new StringBuilder();
        // 使用 AtomicBoolean 确保只保存一次
        AtomicBoolean saved = new AtomicBoolean(false);
        // 🔑 关键：记录前端连接是否已断开（用户刷新）
        AtomicBoolean sinkCancelled = new AtomicBoolean(false);

        return Flux.<String>create(sink -> {
            // 🔑 关键：当用户刷新时，只标记连接断开，不立即保存
            // 等待原始 AI 流完成后再保存完整内容
            sink.onCancel(() -> {
                log.info("App {} (类型:{}) 检测到前端连接取消，等待 AI 完成后保存", appId, codeGenType.getValue());
                sinkCancelled.set(true);
                // 不在这里保存！等原始流完成后再保存完整内容
            });

            sink.onDispose(() -> {
                log.info("App {} (类型:{}) Sink 被销毁", appId, codeGenType.getValue());
                sinkCancelled.set(true);
                // 不在这里保存！等原始流完成后再保存完整内容
            });

            // 订阅原始 AI 流（这个流会独立运行，不受前端连接影响）
            codeStream.subscribe(
                chunk -> {
                    // 🔑 关键：始终收集响应内容，无论前端是否断开
                    responseBuilder.append(chunk);
                    // 只有前端未断开时才转发
                    if (!sinkCancelled.get()) {
                        try {
                            sink.next(chunk);
                        } catch (Exception e) {
                            log.debug("发送响应失败（连接可能已断开）: {}", e.getMessage());
                            sinkCancelled.set(true);
                        }
                    }
                },
                error -> {
                    log.error("App {} (类型:{}) AI 生成出错", appId, codeGenType.getValue(), error);
                    // 🔑 关键：AI 出错时保存已收集的内容
                    if (saved.compareAndSet(false, true)) {
                        saveCodeStreamResponse(appId, userId, responseBuilder.toString(), codeGenType);
                    }
                    if (!sinkCancelled.get()) {
                        try {
                            sink.error(error);
                        } catch (Exception e) {
                            log.debug("发送错误失败（连接可能已断开）: {}", e.getMessage());
                        }
                    }
                },
                () -> {
                    // 🔑 关键：AI 流完成时保存完整内容（无论前端是否还连接）
                    log.info("App {} (类型:{}) AI 响应完成，保存完整内容", appId, codeGenType.getValue());
                    if (saved.compareAndSet(false, true)) {
                        saveCodeStreamResponse(appId, userId, responseBuilder.toString(), codeGenType);
                    }
                    if (!sinkCancelled.get()) {
                        try {
                            sink.complete();
                        } catch (Exception e) {
                            log.debug("完成流失败（连接可能已断开）: {}", e.getMessage());
                        }
                    }
                }
            );
        });
    }

    /**
     * 保存 HTML/MULTI_FILE 类型的完整响应到数据库，并保存代码文件
     *
     * @param appId       应用ID
     * @param userId      用户ID
     * @param response    完整响应内容
     * @param codeGenType 代码生成类型
     */
    private void saveCodeStreamResponse(Long appId, Long userId, String response, CodeGenTypeEnum codeGenType) {
        try {
            // 1. 保存 AI 响应到数据库
            if (response != null && !response.isBlank()) {
                chatHistoryService.addChatMessage(appId, response, "ai", userId);
                log.info("App {} (类型:{}) 保存完整 AI 消息到数据库", appId, codeGenType.getValue());
            } else {
                log.warn("App {} (类型:{}) AI 响应为空，跳过保存到数据库", appId, codeGenType.getValue());
            }

            // 2. 保存代码文件
            if (response != null && !response.isBlank()) {
                try {
                    Object parsedResult = CodeParserExecutor.executeParser(response, codeGenType);
                    File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType, appId);
                    log.info("App {} (类型:{}) 代码文件保存成功，路径: {}", appId, codeGenType.getValue(), savedDir.getAbsolutePath());
                } catch (Exception e) {
                    log.error("App {} (类型:{}) 代码文件保存失败: {}", appId, codeGenType.getValue(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("App {} (类型:{}) 保存响应失败: {}", appId, codeGenType.getValue(), e.getMessage(), e);
        }
    }


}