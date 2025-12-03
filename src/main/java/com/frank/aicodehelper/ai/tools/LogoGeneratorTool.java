package com.frank.aicodehelper.ai.tools;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Logo 生成工具
 * 提供多种 Logo 生成方式：
 * 1. 文字 Logo（使用 Google Fonts + SVG）
 * 2. 图标 Logo（使用 DiceBear API）
 * 3. 首字母 Logo（使用 UI Avatars API）
 */
@Slf4j
@Component
public class LogoGeneratorTool extends BaseTool {

    @Tool("生成网站 Logo，支持文字Logo、图标Logo、首字母Logo等多种风格")
    public String generateLogo(
            @P("Logo 文本内容，如公司名称或简称")
            String text,
            @P("Logo 风格: 'text' 纯文字, 'icon' 图标风格, 'initial' 首字母, 'badge' 徽章风格")
            String style,
            @P("主题颜色，十六进制格式如 '#6366f1'")
            String primaryColor,
            @P("Logo 尺寸: 'small' 32px, 'medium' 64px, 'large' 128px")
            String size
    ) {
        String cleanColor = primaryColor.replace("#", "");
        int sizeValue = getSizeValue(size);
        
        String logoUrl;
        String logoSvg;
        
        switch (style.toLowerCase()) {
            case "icon" -> {
                // 使用 DiceBear API 生成抽象图标风格 Logo
                logoUrl = String.format(
                        "https://api.dicebear.com/7.x/shapes/svg?seed=%s&backgroundColor=%s&size=%d",
                        URLEncoder.encode(text, StandardCharsets.UTF_8),
                        cleanColor,
                        sizeValue
                );
                logoSvg = null;
            }
            case "initial" -> {
                // 使用 UI Avatars 生成首字母 Logo
                String initials = getInitials(text);
                logoUrl = String.format(
                        "https://ui-avatars.com/api/?name=%s&background=%s&color=fff&size=%d&bold=true&format=svg",
                        URLEncoder.encode(initials, StandardCharsets.UTF_8),
                        cleanColor,
                        sizeValue
                );
                logoSvg = null;
            }
            case "badge" -> {
                // 生成徽章风格的 SVG Logo
                logoSvg = generateBadgeSvg(text, primaryColor, sizeValue);
                logoUrl = null;
            }
            case "text" -> {
                // 生成纯文字 SVG Logo
                logoSvg = generateTextSvg(text, primaryColor, sizeValue);
                logoUrl = null;
            }
            default -> {
                // 默认使用首字母风格
                String initials = getInitials(text);
                logoUrl = String.format(
                        "https://ui-avatars.com/api/?name=%s&background=%s&color=fff&size=%d&bold=true&format=svg",
                        URLEncoder.encode(initials, StandardCharsets.UTF_8),
                        cleanColor,
                        sizeValue
                );
                logoSvg = null;
            }
        }

        StringBuilder result = new StringBuilder();
        result.append("Logo 生成成功:\n");
        result.append(String.format("- 文本: %s\n", text));
        result.append(String.format("- 风格: %s\n", style));
        result.append(String.format("- 颜色: %s\n", primaryColor));
        result.append(String.format("- 尺寸: %dpx\n\n", sizeValue));
        
        if (logoUrl != null) {
            result.append("Logo URL:\n").append(logoUrl);
        } else if (logoSvg != null) {
            result.append("Logo SVG 代码:\n```svg\n").append(logoSvg).append("\n```");
        }

        log.info("Logo 生成成功，文本: {}, 风格: {}", text, style);
        return result.toString();
    }

    /**
     * 获取尺寸像素值
     */
    private int getSizeValue(String size) {
        return switch (size.toLowerCase()) {
            case "small" -> 32;
            case "large" -> 128;
            default -> 64;
        };
    }

    /**
     * 获取首字母
     */
    private String getInitials(String text) {
        if (text == null || text.isBlank()) {
            return "?";
        }
        String[] words = text.trim().split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        }
        return (words[0].charAt(0) + "" + words[words.length - 1].charAt(0)).toUpperCase();
    }

    /**
     * 生成徽章风格 SVG
     */
    private String generateBadgeSvg(String text, String color, int size) {
        String initials = getInitials(text);
        int fontSize = size / 3;
        int radius = size / 8;
        
        return String.format("""
                <svg width="%d" height="%d" viewBox="0 0 %d %d" xmlns="http://www.w3.org/2000/svg">
                  <rect width="%d" height="%d" rx="%d" fill="%s"/>
                  <text x="50%%" y="50%%" dominant-baseline="central" text-anchor="middle" 
                        fill="white" font-family="Arial, sans-serif" font-weight="bold" font-size="%d">
                    %s
                  </text>
                </svg>
                """, size, size, size, size, size, size, radius, color, fontSize, initials);
    }

    /**
     * 生成纯文字 SVG
     */
    private String generateTextSvg(String text, String color, int size) {
        int fontSize = size / 2;
        int width = text.length() * fontSize + 20;
        
        return String.format("""
                <svg width="%d" height="%d" viewBox="0 0 %d %d" xmlns="http://www.w3.org/2000/svg">
                  <text x="10" y="50%%" dominant-baseline="central" 
                        fill="%s" font-family="'Segoe UI', Arial, sans-serif" font-weight="bold" font-size="%d">
                    %s
                  </text>
                </svg>
                """, width, size, width, size, color, fontSize, text);
    }

    @Override
    public String getToolName() {
        return "generateLogo";
    }

    @Override
    public String getDisplayName() {
        return "生成Logo";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String text = arguments.getStr("text", "Logo");
        String style = arguments.getStr("style", "initial");
        return String.format("[工具调用] %s - 文本: %s, 风格: %s", getDisplayName(), text, style);
    }
}

