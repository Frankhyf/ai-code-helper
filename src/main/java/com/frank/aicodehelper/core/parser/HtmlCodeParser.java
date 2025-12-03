package com.frank.aicodehelper.core.parser;

import com.frank.aicodehelper.ai.model.HtmlCodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML 单文件代码解析器
 *
 */
@Slf4j
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {

    // 标准 Markdown 代码块模式: ```html ... ```
    private static final Pattern HTML_CODE_BLOCK_PATTERN = Pattern.compile(
            "```html\\s*\\n([\\s\\S]*?)```",
            Pattern.CASE_INSENSITIVE
    );

    // 通用代码块模式: ``` ... ``` (不带语言标识)
    private static final Pattern GENERIC_CODE_BLOCK_PATTERN = Pattern.compile(
            "```\\s*\\n([\\s\\S]*?)```"
    );

    // 直接提取 HTML 文档模式: 从 <!DOCTYPE 或 <html 开始到 </html> 结束
    private static final Pattern HTML_DOCUMENT_PATTERN = Pattern.compile(
            "(<!DOCTYPE[^>]*>\\s*)?(<html[\\s\\S]*?</html>)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            log.warn("未能从AI响应中提取HTML代码块，响应长度: {}", codeContent.length());
            // 尝试直接提取HTML文档
            String directHtml = extractHtmlDocument(codeContent);
            if (directHtml != null && !directHtml.trim().isEmpty()) {
                result.setHtmlCode(directHtml.trim());
            } else {
                // 最后的 fallback：记录警告并使用原始内容
                log.error("无法从AI响应中提取有效的HTML代码");
                result.setHtmlCode(codeContent.trim());
            }
        }
        return result;
    }

    /**
     * 提取HTML代码内容（从代码块中）
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractHtmlCode(String content) {
        // 1. 首先尝试标准的 ```html 代码块
        Matcher htmlMatcher = HTML_CODE_BLOCK_PATTERN.matcher(content);
        if (htmlMatcher.find()) {
            return htmlMatcher.group(1);
        }

        // 2. 尝试通用代码块 ```，检查内容是否包含HTML
        Matcher genericMatcher = GENERIC_CODE_BLOCK_PATTERN.matcher(content);
        if (genericMatcher.find()) {
            String blockContent = genericMatcher.group(1);
            // 验证是否包含HTML内容
            if (blockContent.contains("<html") || blockContent.contains("<!DOCTYPE")) {
                return blockContent;
            }
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
