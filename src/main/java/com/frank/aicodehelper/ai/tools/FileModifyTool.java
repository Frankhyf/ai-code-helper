package com.frank.aicodehelper.ai.tools;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * 文件修改工具
 * 支持 AI 通过工具调用的方式修改文件内容
 */
@Slf4j
@Component
public class FileModifyTool extends BaseTool{

    @Tool("修改文件内容，用新内容替换指定的旧内容")
    public String modifyFile(
            @P("文件的相对路径")
            String relativeFilePath,
            @P("要替换的旧内容")
            String oldContent,
            @P("替换后的新内容")
            String newContent,
            @ToolMemoryId Long appId
    ) {
        log.info("=== 开始执行文件修改 ===");
        log.info("appId: {}, 文件路径: {}", appId, relativeFilePath);
        log.info("oldContent 长度: {}, newContent 长度: {}", 
                oldContent != null ? oldContent.length() : 0, 
                newContent != null ? newContent.length() : 0);
        
        // 【关键检查】防止无限循环：oldContent 和 newContent 相同时直接拒绝
        if (oldContent != null && oldContent.equals(newContent)) {
            log.warn("=== 检测到无效调用：oldContent 和 newContent 完全相同 ===");
            return "错误：替换前后内容完全相同，无需修改。如果任务已完成，请直接告知用户。";
        }
        
        try {
            // 使用通用方法解析文件路径（自动检测项目类型）
            Path path = resolveFilePath(relativeFilePath, appId);
            log.info("解析后的完整路径: {}", path.toAbsolutePath());
            
            if (!Files.exists(path)) {
                log.error("文件不存在: {}", path.toAbsolutePath());
                return "错误：文件不存在 - " + relativeFilePath + " (完整路径: " + path.toAbsolutePath() + ")";
            }
            if (!Files.isRegularFile(path)) {
                log.error("不是文件: {}", path.toAbsolutePath());
                return "错误：不是文件 - " + relativeFilePath;
            }
            
            String originalContent = Files.readString(path);
            log.info("文件内容长度: {}", originalContent.length());
            
            if (!originalContent.contains(oldContent)) {
                // 添加调试日志，帮助排查匹配问题
                log.warn("=== 文件修改失败 - 未找到匹配内容 ===");
                log.warn("文件路径: {}", path.toAbsolutePath());
                log.warn("要查找的内容长度: {}", oldContent.length());
                log.warn("要查找的内容（前200字符）: [{}]", oldContent.length() > 200 ? oldContent.substring(0, 200) + "..." : oldContent);
                log.warn("文件内容长度: {}", originalContent.length());
                // 尝试查找部分匹配，帮助诊断
                String firstLine = oldContent.split("\n")[0];
                if (originalContent.contains(firstLine)) {
                    log.warn("文件中包含第一行内容，可能是空格/换行问题");
                } else {
                    log.warn("文件中不包含第一行内容: [{}]", firstLine);
                }
                return "警告：文件中未找到要替换的内容，文件未修改 - " + relativeFilePath;
            }
            
            String modifiedContent = originalContent.replace(oldContent, newContent);
            if (originalContent.equals(modifiedContent)) {
                log.warn("替换后内容未变化");
                return "信息：替换后文件内容未发生变化 - " + relativeFilePath;
            }

            Files.writeString(path, modifiedContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("=== 成功修改文件: {} ===", path.toAbsolutePath());

            // 轻量级代码验证
            List<String> validationErrors = CodeQuickValidator.validate(relativeFilePath, modifiedContent);
            String validationMsg = CodeQuickValidator.formatResult(validationErrors);

            if (validationMsg != null) {
                log.warn("代码验证警告 [{}]: {}", relativeFilePath, validationErrors);
                return "文件修改成功: " + relativeFilePath + "\n" + validationMsg;
            }
            return "文件修改成功: " + relativeFilePath;
        } catch (IOException e) {
            String errorMessage = "修改文件失败: " + relativeFilePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "modifyFile";
    }

    @Override
    public String getDisplayName() {
        return "修改文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String oldContent = arguments.getStr("oldContent");
        String newContent = arguments.getStr("newContent");
        // 显示对比内容
        return String.format("""
                [工具调用] %s %s

                替换前：
                ```
                %s
                ```

                替换后：
                ```
                %s
                ```
                """, getDisplayName(), relativeFilePath, oldContent, newContent);
    }
}
