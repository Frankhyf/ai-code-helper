package com.frank.aicodehelper.ai.tools;

import cn.hutool.json.JSONObject;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Undraw 风格插画工具
 * 提供扁平化 SVG 插画 URL
 * 基于 unDraw (https://undraw.co/) 风格的插画资源
 */
@Slf4j
@Component
public class UndrawIllustrationTool extends BaseTool {

    /**
     * 预定义的插画分类和对应的 CDN 资源
     * 使用 undraw.co 的 SVG 资源（通过 CDN 访问）
     */
    private static final Map<String, String[]> ILLUSTRATION_CATEGORIES = new HashMap<>();

    static {
        // 商业/办公类插画
        ILLUSTRATION_CATEGORIES.put("business", new String[]{
                "https://illustrations.popsy.co/amber/man-on-a-laptop.svg",
                "https://illustrations.popsy.co/amber/business-deal.svg",
                "https://illustrations.popsy.co/amber/woman-with-a-laptop.svg",
                "https://illustrations.popsy.co/amber/remote-work.svg",
                "https://illustrations.popsy.co/amber/presentation.svg"
        });
        
        // 技术/开发类插画
        ILLUSTRATION_CATEGORIES.put("technology", new String[]{
                "https://illustrations.popsy.co/amber/developer.svg",
                "https://illustrations.popsy.co/amber/coding.svg",
                "https://illustrations.popsy.co/amber/cloud-computing.svg",
                "https://illustrations.popsy.co/amber/web-development.svg",
                "https://illustrations.popsy.co/amber/app-development.svg"
        });
        
        // 团队协作类插画
        ILLUSTRATION_CATEGORIES.put("team", new String[]{
                "https://illustrations.popsy.co/amber/team-work.svg",
                "https://illustrations.popsy.co/amber/discussion.svg",
                "https://illustrations.popsy.co/amber/collaboration.svg",
                "https://illustrations.popsy.co/amber/meeting.svg",
                "https://illustrations.popsy.co/amber/brainstorming.svg"
        });
        
        // 成功/成就类插画
        ILLUSTRATION_CATEGORIES.put("success", new String[]{
                "https://illustrations.popsy.co/amber/success.svg",
                "https://illustrations.popsy.co/amber/celebration.svg",
                "https://illustrations.popsy.co/amber/trophy.svg",
                "https://illustrations.popsy.co/amber/achievement.svg",
                "https://illustrations.popsy.co/amber/growth.svg"
        });
        
        // 教育/学习类插画
        ILLUSTRATION_CATEGORIES.put("education", new String[]{
                "https://illustrations.popsy.co/amber/reading.svg",
                "https://illustrations.popsy.co/amber/studying.svg",
                "https://illustrations.popsy.co/amber/online-learning.svg",
                "https://illustrations.popsy.co/amber/graduation.svg",
                "https://illustrations.popsy.co/amber/knowledge.svg"
        });
        
        // 空状态/错误类插画
        ILLUSTRATION_CATEGORIES.put("empty", new String[]{
                "https://illustrations.popsy.co/amber/no-data.svg",
                "https://illustrations.popsy.co/amber/empty-box.svg",
                "https://illustrations.popsy.co/amber/not-found.svg",
                "https://illustrations.popsy.co/amber/error.svg",
                "https://illustrations.popsy.co/amber/searching.svg"
        });
        
        // 通用/默认类插画
        ILLUSTRATION_CATEGORIES.put("general", new String[]{
                "https://illustrations.popsy.co/amber/creative-thinking.svg",
                "https://illustrations.popsy.co/amber/idea.svg",
                "https://illustrations.popsy.co/amber/innovation.svg",
                "https://illustrations.popsy.co/amber/design.svg",
                "https://illustrations.popsy.co/amber/solution.svg"
        });
    }

    @Tool("获取扁平化 SVG 插画，适用于网站装饰、空状态、功能介绍等场景")
    public String getIllustration(
            @P("插画类别: 'business'商业办公, 'technology'技术开发, 'team'团队协作, 'success'成功成就, 'education'教育学习, 'empty'空状态, 'general'通用")
            String category,
            @P("需要的插画数量，建议1-3个")
            int count,
            @P("主题颜色，如 '#6366f1'，将影响插画主色调")
            String primaryColor
    ) {
        String normalizedCategory = category.toLowerCase().trim();
        String[] illustrations = ILLUSTRATION_CATEGORIES.getOrDefault(normalizedCategory, 
                ILLUSTRATION_CATEGORIES.get("general"));
        
        StringBuilder result = new StringBuilder();
        result.append("获取到以下插画资源:\n");
        
        int actualCount = Math.min(count, illustrations.length);
        for (int i = 0; i < actualCount; i++) {
            String url = illustrations[i];
            // 如果需要改变颜色，可以通过 CSS filter 或 SVG 处理
            result.append(String.format("%d. %s\n", i + 1, url));
        }
        
        result.append("\n提示: 这些是 SVG 格式插画，可以通过 CSS 调整颜色。");
        if (primaryColor != null && !primaryColor.isBlank()) {
            result.append(String.format("\n建议使用 CSS filter 或直接修改 SVG fill 属性应用主题色 %s", primaryColor));
        }
        
        log.info("获取插画成功，类别: {}, 数量: {}", category, actualCount);
        return result.toString();
    }

    @Override
    public String getToolName() {
        return "getIllustration";
    }

    @Override
    public String getDisplayName() {
        return "获取插画";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String category = arguments.getStr("category", "general");
        int count = arguments.getInt("count", 1);
        return String.format("[工具调用] %s - 类别: %s, 数量: %d", getDisplayName(), category, count);
    }
}

