// 文件路径: main/java/com/frank/aicodehelper/ai/tools/CodeQuickValidator.java
package com.frank.aicodehelper.ai.tools;

import cn.hutool.core.io.FileUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * 轻量级代码快速验证器
 * 只做基础语法检查，耗时控制在 1-5ms
 */
public class CodeQuickValidator {

    /**
     * 根据文件扩展名自动选择验证逻辑
     * @return 错误列表，为空表示通过
     */
    public static List<String> validate(String filePath, String content) {
        List<String> errors = new ArrayList<>();
        String ext = FileUtil.getSuffix(filePath).toLowerCase();
        
        switch (ext) {
            case "vue" -> validateVue(content, errors);
            case "js", "ts" -> validateBrackets(content, errors);
            case "css", "scss" -> validateCssBraces(content, errors);
            case "html" -> validateHtmlBasic(content, errors);
            case "json" -> validateJsonBrackets(content, errors);
        }
        
        return errors;
    }

    /**
     * Vue 文件快速检查
     */
    private static void validateVue(String content, List<String> errors) {
        // 检查必须有 template
        if (!content.contains("<template")) {
            errors.add("缺少 <template> 标签");
        }
        // 检查 template 闭合
        if (content.contains("<template") && !content.contains("</template>")) {
            errors.add("<template> 标签未闭合");
        }
        // 检查 script 闭合
        if (content.contains("<script") && !content.contains("</script>")) {
            errors.add("<script> 标签未闭合");
        }
        // 检查 style 闭合
        if (content.contains("<style") && !content.contains("</style>")) {
            errors.add("<style> 标签未闭合");
        }
        // 检查 JS 括号平衡
        validateBrackets(content, errors);
    }

    /**
     * 括号平衡检查（JS/TS）
     */
    private static void validateBrackets(String content, List<String> errors) {
        int braces = 0;      // {}
        int brackets = 0;    // []
        int parens = 0;      // ()
        boolean inString = false;
        char stringChar = 0;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            char prev = i > 0 ? content.charAt(i - 1) : 0;
            
            // 跳过字符串内容
            if ((c == '"' || c == '\'' || c == '`') && prev != '\\') {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
                continue;
            }
            if (inString) continue;
            
            switch (c) {
                case '{' -> braces++;
                case '}' -> braces--;
                case '[' -> brackets++;
                case ']' -> brackets--;
                case '(' -> parens++;
                case ')' -> parens--;
            }
        }
        
        if (braces != 0) errors.add("花括号 {} 不匹配 (差值: " + braces + ")");
        if (brackets != 0) errors.add("方括号 [] 不匹配 (差值: " + brackets + ")");
        if (parens != 0) errors.add("圆括号 () 不匹配 (差值: " + parens + ")");
    }

    /**
     * CSS 花括号检查
     */
    private static void validateCssBraces(String content, List<String> errors) {
        int braces = 0;
        for (char c : content.toCharArray()) {
            if (c == '{') braces++;
            if (c == '}') braces--;
        }
        if (braces != 0) {
            errors.add("CSS 花括号 {} 不匹配 (差值: " + braces + ")");
        }
    }

    /**
     * HTML 基础检查
     */
    private static void validateHtmlBasic(String content, List<String> errors) {
        // 检查常见标签闭合
        String[] checkTags = {"div", "span", "section", "header", "footer", "main", "nav", "ul", "li"};
        for (String tag : checkTags) {
            int openCount = countOccurrences(content, "<" + tag);
            int closeCount = countOccurrences(content, "</" + tag + ">");
            if (openCount > closeCount) {
                errors.add("<" + tag + "> 标签可能未闭合 (开: " + openCount + ", 闭: " + closeCount + ")");
                break; // 只报告第一个问题
            }
        }
    }

    /**
     * JSON 括号检查
     */
    private static void validateJsonBrackets(String content, List<String> errors) {
        validateBrackets(content, errors);
    }

    private static int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * 格式化验证结果
     */
    public static String formatResult(List<String> errors) {
        if (errors.isEmpty()) {
            return null;
        }
        return "⚠️ 语法检查: " + String.join("; ", errors);
    }
}