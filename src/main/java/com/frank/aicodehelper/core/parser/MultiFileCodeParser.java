package com.frank.aicodehelper.core.parser;

import com.frank.aicodehelper.ai.model.MultiFileCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多文件代码解析器（HTML + CSS + JS）
 *
 */
@Slf4j
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

    // 标准 Markdown 代码块模式
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    // Fallback: 直接提取 HTML 文档模式
    private static final Pattern HTML_DOCUMENT_PATTERN = Pattern.compile(
            "(<!DOCTYPE[^>]*>\\s*)?(<html[\\s\\S]*?</html>)",
            Pattern.CASE_INSENSITIVE
    );

    // Fallback: 提取 <style> 标签内的 CSS
    private static final Pattern STYLE_TAG_PATTERN = Pattern.compile(
            "<style[^>]*>([\\s\\S]*?)</style>",
            Pattern.CASE_INSENSITIVE
    );

    // Fallback: 提取 <script> 标签内的 JS
    private static final Pattern SCRIPT_TAG_PATTERN = Pattern.compile(
            "<script[^>]*>([\\s\\S]*?)</script>",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();

        // 提取各类代码（优先使用代码块格式）
        String htmlCode = extractCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, JS_CODE_PATTERN);

        // 如果没有找到 HTML 代码块，尝试直接提取 HTML 文档
        if (htmlCode == null || htmlCode.trim().isEmpty()) {
            log.warn("未找到HTML代码块，尝试直接提取HTML文档");
            htmlCode = extractHtmlDocument(codeContent);
        }

        // 设置HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            log.error("无法从AI响应中提取有效的HTML代码");
        }

        // 设置CSS代码
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }

        // 设置JS代码
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }

        return result;
    }

    /**
     * 根据正则模式提取代码
     *
     * @param content 原始内容
     * @param pattern 正则模式
     * @return 提取的代码
     */
    private String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 直接从文本中提取HTML文档
     * 用于处理AI没有使用代码块格式的情况
     *
     * @param content 原始内容
     * @return HTML文档代码
     */
    private String extractHtmlDocument(String content) {
        Matcher matcher = HTML_DOCUMENT_PATTERN.matcher(content);
        if (matcher.find()) {
            String doctype = matcher.group(1);
            String htmlContent = matcher.group(2);
            if (doctype != null) {
                return doctype + htmlContent;
            }
            return htmlContent;
        }
        return null;
    }
}
