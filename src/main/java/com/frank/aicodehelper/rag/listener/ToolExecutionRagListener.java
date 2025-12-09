package com.frank.aicodehelper.rag.listener;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.frank.aicodehelper.rag.service.ProjectContextService;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 工具执行 RAG 监听器
 * 在文件写入/修改/删除后触发向量索引更新
 * 实现方案A：工具执行后实时（异步）索引
 */
@Slf4j
@Component
public class ToolExecutionRagListener {

    @Resource
    private ProjectContextService projectContextService;

    /**
     * 处理工具执行事件
     * 异步执行，不阻塞主流程
     *
     * @param toolExecution 工具执行信息
     * @param appId         应用ID
     */
    @Async
    public void onToolExecuted(ToolExecution toolExecution, Long appId) {
        if (!projectContextService.isEnabled()) {
            return;
        }

        String toolName = toolExecution.request().name();
        String arguments = toolExecution.request().arguments();

        try {
            switch (toolName) {
                case "writeFile" -> handleWriteFile(appId, arguments);
                case "modifyFile" -> handleModifyFile(appId, arguments, toolExecution.result());
                case "deleteFile" -> handleDeleteFile(appId, arguments);
                default -> {
                    // 其他工具不触发索引更新
                    log.trace("工具 {} 不触发 RAG 索引更新", toolName);
                }
            }
        } catch (Exception e) {
            log.error("RAG 索引更新失败: appId={}, tool={}, error={}",
                    appId, toolName, e.getMessage(), e);
        }
    }

    /**
     * 处理 writeFile 工具执行
     * 从工具参数中提取文件路径和内容，触发索引
     */
    private void handleWriteFile(Long appId, String arguments) {
        JSONObject args = JSONUtil.parseObj(arguments);
        String filePath = args.getStr("relativeFilePath");
        String content = args.getStr("content");

        if (filePath == null || content == null) {
            log.warn("writeFile 参数不完整: {}", arguments);
            return;
        }

        log.info("📥 [RAG索引] 触发索引更新 [writeFile]: appId={}, file={}, 内容长度={}",
                appId, filePath, content.length());
        projectContextService.indexCodeFile(appId, filePath, content);
    }

    /**
     * 处理 modifyFile 工具执行
     * modifyFile 通常只替换部分内容，需要重新读取整个文件来更新索引
     * 这里从工具参数中获取修改后的内容
     */
    private void handleModifyFile(Long appId, String arguments, String result) {
        JSONObject args = JSONUtil.parseObj(arguments);
        // modifyFile 工具参数名称为 relativeFilePath，这里兼容 filePath/relativeFilePath 两种写法
        String filePath = args.getStr("relativeFilePath", args.getStr("filePath"));

        // modifyFile 的参数结构：relativeFilePath, oldContent, newContent
        // 我们需要获取修改后的完整文件内容
        // 但工具执行后的 result 通常只是成功提示，不含完整内容
        // 所以这里需要从文件系统重新读取

        if (filePath == null) {
            log.warn("modifyFile 参数不完整: {}", arguments);
            return;
        }

        log.info("📝 [RAG索引] 触发索引更新 [modifyFile]: appId={}, file={}", appId, filePath);

        // 从文件系统读取最新内容
        String content = readFileFromDisk(appId, filePath);
        if (content != null) {
            log.info("📝 [RAG索引] 已读取修改后文件内容: file={}, 长度={}", filePath, content.length());
            projectContextService.indexCodeFile(appId, filePath, content);
        }
    }

    /**
     * 处理 deleteFile 工具执行
     */
    private void handleDeleteFile(Long appId, String arguments) {
        JSONObject args = JSONUtil.parseObj(arguments);
        String filePath = args.getStr("relativeFilePath");

        if (filePath == null) {
            log.warn("deleteFile 参数不完整: {}", arguments);
            return;
        }

        log.info("🗑️ [RAG索引] 触发索引删除 [deleteFile]: appId={}, file={}", appId, filePath);
        projectContextService.deleteByFilePath(appId, filePath);
    }

    /**
     * 从磁盘读取文件内容
     * 用于 modifyFile 后获取最新内容
     */
    private String readFileFromDisk(Long appId, String relativeFilePath) {
        try {
            // 构建文件路径（与 BaseTool.resolveFilePath 逻辑一致）
            String codeOutputDir = System.getProperty("user.dir") + "/tmp/code_output";
            String[] prefixes = {"vue_project_", "html_", "multi_file_"};

            for (String prefix : prefixes) {
                java.nio.file.Path projectRoot = java.nio.file.Paths.get(codeOutputDir, prefix + appId);
                if (java.nio.file.Files.exists(projectRoot)) {
                    java.nio.file.Path filePath = projectRoot.resolve(relativeFilePath);
                    if (java.nio.file.Files.exists(filePath) && java.nio.file.Files.isRegularFile(filePath)) {
                        return java.nio.file.Files.readString(filePath);
                    }
                }
            }

            log.warn("文件不存在: appId={}, file={}", appId, relativeFilePath);
            return null;
        } catch (Exception e) {
            log.error("读取文件失败: appId={}, file={}, error={}",
                    appId, relativeFilePath, e.getMessage());
            return null;
        }
    }
}


