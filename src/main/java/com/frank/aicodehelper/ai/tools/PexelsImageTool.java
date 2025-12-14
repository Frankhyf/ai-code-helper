package com.frank.aicodehelper.ai.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.List;

/**
 * Pexels 图片搜索工具（已弃用）
 * 提供高质量免费图片搜索功能
 * API 文档: https://www.pexels.com/api/documentation/
 * 
 * @deprecated 已被 {@link PixabayImageTool} 替代，该工具实现了 Pixabay + Pexels 双源回退机制
 */
@Slf4j
// @Component  // 已禁用，由 PixabayImageTool 统一提供图片搜索能力（优先 Pixabay，回退 Pexels）
@Deprecated
public class PexelsImageTool extends BaseTool {

    private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    @Value("${tools.pexels.api-key:}")
    private String apiKey;

    @Tool("根据关键词搜索高质量图片，返回图片URL列表")
    public String searchImages(
            @P("搜索关键词，建议使用英文，如 'business office', 'nature landscape', 'technology'")
            String query,
            @P("需要返回的图片数量，建议1-5张")
            int count,
            @P("图片用途：'hero' 横幅大图, 'card' 卡片图, 'avatar' 头像, 'background' 背景图")
            String purpose
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Pexels API Key 未配置，返回占位图");
            return generatePlaceholderImages(query, count, purpose);
        }

        try {
            // 根据用途确定图片尺寸偏好
            String orientation = getOrientationByPurpose(purpose);
            
            HttpResponse response = HttpRequest.get(PEXELS_API_URL)
                    .header("Authorization", apiKey)
                    .form("query", query)
                    .form("per_page", Math.min(count, 15))
                    .form("orientation", orientation)
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray photos = result.getJSONArray("photos");
                
                if (photos == null || photos.isEmpty()) {
                    log.info("Pexels 未找到图片，使用占位图: {}", query);
                    return generatePlaceholderImages(query, count, purpose);
                }

                List<String> imageUrls = new ArrayList<>();
                String sizeKey = getSizeKeyByPurpose(purpose);
                
                for (int i = 0; i < Math.min(count, photos.size()); i++) {
                    JSONObject photo = photos.getJSONObject(i);
                    JSONObject src = photo.getJSONObject("src");
                    String url = src.getStr(sizeKey, src.getStr("medium"));
                    imageUrls.add(url);
                }

                String resultStr = String.join("\n", imageUrls);
                log.info("Pexels 搜索成功，关键词: {}, 返回 {} 张图片", query, imageUrls.size());
                return "搜索到以下图片URL:\n" + resultStr;
            } else {
                log.error("Pexels API 请求失败: {}", response.body());
                return generatePlaceholderImages(query, count, purpose);
            }
        } catch (Exception e) {
            log.error("Pexels 图片搜索异常", e);
            return generatePlaceholderImages(query, count, purpose);
        }
    }

    /**
     * 根据用途获取图片方向
     */
    private String getOrientationByPurpose(String purpose) {
        return switch (purpose.toLowerCase()) {
            case "hero", "background" -> "landscape";
            case "avatar" -> "square";
            case "card" -> "portrait";
            default -> "landscape";
        };
    }

    /**
     * 根据用途获取图片尺寸键
     */
    private String getSizeKeyByPurpose(String purpose) {
        return switch (purpose.toLowerCase()) {
            case "hero", "background" -> "large2x";
            case "avatar" -> "small";
            case "card" -> "medium";
            default -> "large";
        };
    }

    /**
     * 生成占位图片URL
     */
    private String generatePlaceholderImages(String query, int count, String purpose) {
        List<String> urls = new ArrayList<>();
        int width = 800, height = 600;
        
        switch (purpose.toLowerCase()) {
            case "hero", "background" -> { width = 1920; height = 1080; }
            case "avatar" -> { width = 200; height = 200; }
            case "card" -> { width = 400; height = 300; }
        }

        for (int i = 0; i < count; i++) {
            // 使用 picsum.photos 作为备用占位图服务
            String url = String.format("https://picsum.photos/%d/%d?random=%d", width, height, System.currentTimeMillis() + i);
            urls.add(url);
        }
        
        return "使用占位图片:\n" + String.join("\n", urls);
    }

    @Override
    public String getToolName() {
        return "searchImages";
    }

    @Override
    public String getDisplayName() {
        return "搜索图片";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String query = arguments.getStr("query");
        int count = arguments.getInt("count", 1);
        String purpose = arguments.getStr("purpose", "card");
        return String.format("[工具调用] %s - 关键词: %s, 数量: %d, 用途: %s", 
                getDisplayName(), query, count, purpose);
    }
}

