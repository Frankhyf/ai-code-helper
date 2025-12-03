package com.frank.aicodehelper.ai.tools;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码质量检查工具
 * 提供代码语法检查、结构验证、最佳实践检测等功能
 */
@Slf4j
@Component
public class CodeValidatorTool extends BaseTool {

    @Tool("检查代码质量，包括语法错误、结构问题、最佳实践等")
    public String validateCode(
            @P("要检查的代码内容")
            String code,
            @P("代码语言: 'html', 'css', 'javascript', 'vue', 'json'")
            String language,
            @P("检查级别: 'basic' 基础语法, 'standard' 标准检查, 'strict' 严格检查")
            String level,
            @ToolMemoryId Long appId
    ) {
        List<ValidationError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();

        switch (language.toLowerCase()) {
            case "html" -> validateHtml(code, level, errors, warnings, suggestions);
            case "css" -> validateCss(code, level, errors, warnings, suggestions);
            case "javascript", "js" -> validateJavaScript(code, level, errors, warnings, suggestions);
            case "vue" -> validateVue(code, level, errors, warnings, suggestions);
            case "json" -> validateJson(code, level, errors, warnings);
            default -> warnings.add("未知的语言类型: " + language);
        }

        return formatValidationResult(errors, warnings, suggestions);
    }

    /**
     * HTML 代码检查
     */
    private void validateHtml(String code, String level, List<ValidationError> errors, 
                              List<String> warnings, List<String> suggestions) {
        // 检查基本结构
        if (!code.contains("<!DOCTYPE") && !code.contains("<!doctype")) {
            warnings.add("缺少 DOCTYPE 声明");
        }
        
        // 检查标签闭合
        checkTagsClosed(code, errors);
        
        // 检查图片 alt 属性
        Pattern imgPattern = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher imgMatcher = imgPattern.matcher(code);
        while (imgMatcher.find()) {
            String imgTag = imgMatcher.group();
            if (!imgTag.contains("alt=")) {
                warnings.add("img 标签缺少 alt 属性，影响可访问性");
                break;
            }
        }

        // 检查是否有内联样式（标准及以上级别）
        if (!level.equals("basic") && code.contains("style=\"")) {
            suggestions.add("建议将内联样式提取到 CSS 文件中");
        }

        // 检查语义化标签使用（严格模式）
        if (level.equals("strict")) {
            if (!code.contains("<header") && !code.contains("<nav") && !code.contains("<main")) {
                suggestions.add("建议使用语义化标签（header, nav, main, footer 等）");
            }
        }
    }

    /**
     * CSS 代码检查
     */
    private void validateCss(String code, String level, List<ValidationError> errors, 
                             List<String> warnings, List<String> suggestions) {
        // 检查花括号配对
        int openBraces = code.length() - code.replace("{", "").length();
        int closeBraces = code.length() - code.replace("}", "").length();
        if (openBraces != closeBraces) {
            errors.add(new ValidationError("语法错误", "花括号不匹配", 0));
        }

        // 检查分号
        Pattern rulePattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher ruleMatcher = rulePattern.matcher(code);
        while (ruleMatcher.find()) {
            String ruleContent = ruleMatcher.group(1);
            String[] declarations = ruleContent.split("\n");
            for (String decl : declarations) {
                decl = decl.trim();
                if (!decl.isEmpty() && !decl.endsWith(";") && decl.contains(":")) {
                    warnings.add("CSS 声明可能缺少分号: " + decl.substring(0, Math.min(30, decl.length())));
                }
            }
        }

        // 检查 !important 使用（标准及以上级别）
        if (!level.equals("basic")) {
            int importantCount = code.split("!important").length - 1;
            if (importantCount > 3) {
                warnings.add("过度使用 !important (" + importantCount + "次)，可能导致样式难以维护");
            }
        }

        // 检查是否使用 CSS 变量（严格模式建议）
        if (level.equals("strict") && !code.contains("var(--")) {
            suggestions.add("建议使用 CSS 变量（--primary-color 等）提高可维护性");
        }
    }

    /**
     * JavaScript 代码检查
     */
    private void validateJavaScript(String code, String level, List<ValidationError> errors, 
                                    List<String> warnings, List<String> suggestions) {
        // 检查括号配对
        checkBracketBalance(code, errors);

        // 检查 var 使用
        if (code.contains("var ")) {
            warnings.add("使用了 var 声明，建议使用 let 或 const");
        }

        // 检查 console.log
        if (code.contains("console.log")) {
            warnings.add("代码中包含 console.log，生产环境建议移除");
        }

        // 检查未使用 === 
        if (!level.equals("basic")) {
            Pattern eqPattern = Pattern.compile("[^=!]==[^=]");
            if (eqPattern.matcher(code).find()) {
                warnings.add("使用了 == 进行比较，建议使用 === 进行严格比较");
            }
        }

        // 检查异步错误处理（严格模式）
        if (level.equals("strict")) {
            if (code.contains("async") && !code.contains("try") && !code.contains(".catch")) {
                suggestions.add("async 函数建议添加 try-catch 或 .catch() 错误处理");
            }
        }
    }

    /**
     * Vue 单文件组件检查
     */
    private void validateVue(String code, String level, List<ValidationError> errors, 
                             List<String> warnings, List<String> suggestions) {
        // 检查基本结构
        if (!code.contains("<template")) {
            errors.add(new ValidationError("结构错误", "Vue 组件缺少 <template> 部分", 0));
        }
        if (!code.contains("<script")) {
            warnings.add("Vue 组件缺少 <script> 部分");
        }

        // 提取并检查各部分
        Pattern templatePattern = Pattern.compile("<template[^>]*>([\\s\\S]*?)</template>");
        Matcher templateMatcher = templatePattern.matcher(code);
        if (templateMatcher.find()) {
            String templateContent = templateMatcher.group(1);
            validateHtml(templateContent, level, errors, warnings, suggestions);
        }

        Pattern stylePattern = Pattern.compile("<style[^>]*>([\\s\\S]*?)</style>");
        Matcher styleMatcher = stylePattern.matcher(code);
        if (styleMatcher.find()) {
            String styleContent = styleMatcher.group(1);
            validateCss(styleContent, level, errors, warnings, suggestions);
        }

        Pattern scriptPattern = Pattern.compile("<script[^>]*>([\\s\\S]*?)</script>");
        Matcher scriptMatcher = scriptPattern.matcher(code);
        if (scriptMatcher.find()) {
            String scriptContent = scriptMatcher.group(1);
            validateJavaScript(scriptContent, level, errors, warnings, suggestions);
        }

        // 检查 scoped 样式
        if (!level.equals("basic") && code.contains("<style") && !code.contains("<style scoped")) {
            suggestions.add("建议使用 <style scoped> 避免样式污染");
        }
    }

    /**
     * JSON 代码检查
     */
    private void validateJson(String code, String level, List<ValidationError> errors, List<String> warnings) {
        try {
            new JSONObject(code);
        } catch (Exception e) {
            errors.add(new ValidationError("JSON 语法错误", e.getMessage(), 0));
        }
    }

    /**
     * 检查 HTML 标签闭合
     */
    private void checkTagsClosed(String code, List<ValidationError> errors) {
        String[] selfClosingTags = {"br", "hr", "img", "input", "meta", "link", "area", "base", "col", "embed", "param", "source", "track", "wbr"};
        
        Pattern tagPattern = Pattern.compile("<(/?)([a-zA-Z][a-zA-Z0-9]*)");
        Matcher matcher = tagPattern.matcher(code);
        
        java.util.Stack<String> tagStack = new java.util.Stack<>();
        while (matcher.find()) {
            boolean isClosing = matcher.group(1).equals("/");
            String tagName = matcher.group(2).toLowerCase();
            
            // 跳过自闭合标签
            boolean isSelfClosing = false;
            for (String sc : selfClosingTags) {
                if (sc.equals(tagName)) {
                    isSelfClosing = true;
                    break;
                }
            }
            if (isSelfClosing) continue;
            
            if (isClosing) {
                if (tagStack.isEmpty() || !tagStack.peek().equals(tagName)) {
                    errors.add(new ValidationError("标签错误", "标签 </" + tagName + "> 没有匹配的开始标签", 0));
                } else {
                    tagStack.pop();
                }
            } else {
                tagStack.push(tagName);
            }
        }
        
        while (!tagStack.isEmpty()) {
            String unclosed = tagStack.pop();
            errors.add(new ValidationError("标签错误", "标签 <" + unclosed + "> 未闭合", 0));
        }
    }

    /**
     * 检查括号平衡
     */
    private void checkBracketBalance(String code, List<ValidationError> errors) {
        int parentheses = 0, braces = 0, brackets = 0;
        
        for (char c : code.toCharArray()) {
            switch (c) {
                case '(' -> parentheses++;
                case ')' -> parentheses--;
                case '{' -> braces++;
                case '}' -> braces--;
                case '[' -> brackets++;
                case ']' -> brackets--;
            }
        }
        
        if (parentheses != 0) errors.add(new ValidationError("语法错误", "圆括号 () 不匹配", 0));
        if (braces != 0) errors.add(new ValidationError("语法错误", "花括号 {} 不匹配", 0));
        if (brackets != 0) errors.add(new ValidationError("语法错误", "方括号 [] 不匹配", 0));
    }

    /**
     * 格式化检查结果
     */
    private String formatValidationResult(List<ValidationError> errors, 
                                          List<String> warnings, 
                                          List<String> suggestions) {
        StringBuilder result = new StringBuilder();
        result.append("=== 代码检查结果 ===\n\n");
        
        if (errors.isEmpty() && warnings.isEmpty()) {
            result.append("✅ 代码检查通过，未发现问题！\n");
        } else {
            if (!errors.isEmpty()) {
                result.append("❌ 错误 (").append(errors.size()).append("):\n");
                for (ValidationError error : errors) {
                    result.append("  - [").append(error.type).append("] ").append(error.message).append("\n");
                }
                result.append("\n");
            }
            
            if (!warnings.isEmpty()) {
                result.append("⚠️ 警告 (").append(warnings.size()).append("):\n");
                for (String warning : warnings) {
                    result.append("  - ").append(warning).append("\n");
                }
                result.append("\n");
            }
        }
        
        if (!suggestions.isEmpty()) {
            result.append("💡 建议 (").append(suggestions.size()).append("):\n");
            for (String suggestion : suggestions) {
                result.append("  - ").append(suggestion).append("\n");
            }
        }
        
        result.append("\n状态: ").append(errors.isEmpty() ? "通过" : "需要修复");
        
        return result.toString();
    }

    /**
     * 验证错误记录
     */
    private record ValidationError(String type, String message, int line) {}

    @Override
    public String getToolName() {
        return "validateCode";
    }

    @Override
    public String getDisplayName() {
        return "代码检查";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String language = arguments.getStr("language", "html");
        String level = arguments.getStr("level", "standard");
        return String.format("[工具调用] %s - 语言: %s, 级别: %s", getDisplayName(), language, level);
    }
}

