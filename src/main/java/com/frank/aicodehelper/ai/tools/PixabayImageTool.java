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
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 统一图片搜索工具（Pixabay + Pexels 双源回退）
 * 
 * 搜索策略：
 * 1. 优先使用 Pixabay API（国内访问稳定，支持中文，图库丰富）
 * 2. Pixabay 失败时自动回退到 Pexels API
 * 3. 两者都失败时返回占位图
 * 
 * Pixabay 特点：
 * - 超过 2700 万张免费图片资源
 * - 支持中文关键词搜索
 * - 国内访问稳定性较好
 * - 支持多种图片类型：照片、矢量图、插画
 * 
 * Pexels 特点：
 * - 高质量摄影图片
 * - API 响应速度快
 * - 图片风格偏专业摄影
 */
@Slf4j
@Component
public class PixabayImageTool extends BaseTool {

    private static final String PIXABAY_API_URL = "https://pixabay.com/api/";
    private static final String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    @Value("${tools.pixabay.api-key:}")
    private String pixabayApiKey;

    @Value("${tools.pexels.api-key:}")
    private String pexelsApiKey;

    /**
     * 搜索高质量图片（双源回退：Pixabay → Pexels → 占位图）
     * 
     * @param query   搜索关键词，支持中文和英文
     * @param count   需要返回的图片数量
     * @param purpose 图片用途
     * @return 图片URL列表
     */
    @Tool("根据关键词搜索高质量图片，支持中文搜索，返回图片URL列表。优先使用Pixabay（2700万+图片），失败后自动切换Pexels。")
    public String searchImages(
            @P("搜索关键词，支持中文或英文，如 '商务办公', 'business office', '自然风景', 'technology'")
            String query,
            @P("需要返回的图片数量，建议1-5张")
            int count,
            @P("图片用途：'hero' 横幅大图, 'card' 卡片图, 'avatar' 头像, 'background' 背景图")
            String purpose
    ) {
        log.info("🔍 开始搜索图片: query={}, count={}, purpose={}", query, count, purpose);
        log.info("📌 Pixabay API Key 状态: {}", (pixabayApiKey != null && !pixabayApiKey.isBlank()) ? "已配置" : "未配置");
        log.info("📌 Pexels API Key 状态: {}", (pexelsApiKey != null && !pexelsApiKey.isBlank()) ? "已配置" : "未配置");
        
        // 1. 优先尝试 Pixabay
        if (pixabayApiKey != null && !pixabayApiKey.isBlank()) {
            log.info("🌐 尝试 Pixabay 搜索...");
            String pixabayResult = searchFromPixabay(query, count, purpose);
            if (pixabayResult != null) {
                log.info("✅ Pixabay 搜索成功");
                return pixabayResult;
            }
            log.warn("⚠️ Pixabay 搜索失败或无结果，尝试回退到 Pexels");
        } else {
            log.warn("⚠️ Pixabay API Key 未配置，尝试使用 Pexels");
        }

        // 2. 回退到 Pexels
        if (pexelsApiKey != null && !pexelsApiKey.isBlank()) {
            log.info("🌐 尝试 Pexels 搜索...");
            String pexelsResult = searchFromPexels(query, count, purpose);
            if (pexelsResult != null) {
                log.info("✅ Pexels 搜索成功");
                return pexelsResult;
            }
            log.warn("⚠️ Pexels 搜索也失败或无结果，使用占位图");
        } else {
            log.warn("⚠️ Pexels API Key 也未配置，使用占位图");
        }

        // 3. 最终回退到占位图
        log.warn("❌ 所有图片搜索源都失败，返回占位图");
        return generatePlaceholderImages(query, count, purpose);
    }

    /**
     * 搜索矢量图/插画（仅 Pixabay 支持）
     *
     * @param query   搜索关键词
     * @param count   需要返回的数量
     * @param type    类型：'vector' 矢量图, 'illustration' 插画
     * @return 图片URL列表
     */
    @Tool("搜索矢量图或插画，适合用于图标、装饰元素等，返回图片URL列表")
    public String searchVectorOrIllustration(
            @P("搜索关键词，支持中文或英文")
            String query,
            @P("需要返回的数量，建议1-5张")
            int count,
            @P("类型：'vector' 矢量图, 'illustration' 插画")
            String type
    ) {
        if (pixabayApiKey == null || pixabayApiKey.isBlank()) {
            log.warn("Pixabay API Key 未配置，矢量图/插画搜索不可用，返回占位图");
            return generatePlaceholderImages(query, count, "card");
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String imageType = "vector".equalsIgnoreCase(type) ? "vector" : "illustration";
            
            // Pixabay API 要求 per_page 范围为 3-200
            int perPage = Math.max(3, Math.min(count * 2, 30));
            String url = String.format(
                    "%s?key=%s&q=%s&image_type=%s&per_page=%d&safesearch=true&lang=zh",
                    PIXABAY_API_URL, pixabayApiKey, encodedQuery, imageType, perPage
            );

            HttpResponse response = HttpRequest.get(url)
                    .timeout(15000)
                    .execute();

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray hits = result.getJSONArray("hits");
                
                if (hits == null || hits.isEmpty()) {
                    log.info("Pixabay 未找到{}，关键词: {}", imageType, query);
                    return generatePlaceholderImages(query, count, "card");
                }

                List<String> imageUrls = new ArrayList<>();
                for (int i = 0; i < Math.min(count, hits.size()); i++) {
                    JSONObject hit = hits.getJSONObject(i);
                    String imageUrl = hit.getStr("largeImageURL", hit.getStr("webformatURL"));
                    if (imageUrl != null && !imageUrl.isBlank()) {
                        imageUrls.add(imageUrl);
                    }
                }

                if (imageUrls.isEmpty()) {
                    return generatePlaceholderImages(query, count, "card");
                }

                String resultStr = String.join("\n", imageUrls);
                log.info("Pixabay {}搜索成功，关键词: {}, 返回 {} 张", imageType, query, imageUrls.size());
                return "搜索到以下" + (imageType.equals("vector") ? "矢量图" : "插画") + "URL:\n" + resultStr;
            } else {
                log.error("Pixabay API 请求失败: {}", response.body());
                return generatePlaceholderImages(query, count, "card");
            }
        } catch (Exception e) {
            log.error("Pixabay 矢量图/插画搜索异常", e);
            return generatePlaceholderImages(query, count, "card");
        }
    }

    /**
     * 按分类搜索图片（双源回退）
     *
     * @param category 分类名称
     * @param count    需要返回的数量
     * @param purpose  图片用途
     * @return 图片URL列表
     */
    @Tool("按分类搜索图片，支持的分类包括：商业、科技、自然、人物、动物、食物、建筑、交通等")
    public String searchByCategory(
            @P("分类名称：'business' 商业, 'science' 科技, 'nature' 自然, 'people' 人物, 'animals' 动物, 'food' 食物, 'buildings' 建筑, 'travel' 旅行, 'sports' 运动, 'health' 健康, 'education' 教育, 'computer' 电脑, 'music' 音乐, 'fashion' 时尚")
            String category,
            @P("需要返回的图片数量，建议1-5张")
            int count,
            @P("图片用途：'hero' 横幅大图, 'card' 卡片图, 'avatar' 头像, 'background' 背景图")
            String purpose
    ) {
        // 1. 优先尝试 Pixabay（支持分类搜索）
        if (pixabayApiKey != null && !pixabayApiKey.isBlank()) {
            String pixabayResult = searchByCategoryFromPixabay(category, count, purpose);
            if (pixabayResult != null && !pixabayResult.startsWith("使用占位图片")) {
                return pixabayResult;
            }
            log.info("Pixabay 分类搜索失败，回退到 Pexels 关键词搜索");
        }

        // 2. 回退到 Pexels（使用分类名作为关键词）
        if (pexelsApiKey != null && !pexelsApiKey.isBlank()) {
            String pexelsResult = searchFromPexels(category, count, purpose);
            if (pexelsResult != null && !pexelsResult.startsWith("使用占位图片")) {
                return pexelsResult;
            }
        }

        // 3. 最终回退到占位图
        return generatePlaceholderImages(category, count, purpose);
    }

    // ==================== Pixabay 搜索实现 ====================

    /**
     * 从 Pixabay 搜索图片
     */
    private String searchFromPixabay(String query, int count, String purpose) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String imageType = getPixabayImageTypeByPurpose(purpose);
            String orientation = getPixabayOrientationByPurpose(purpose);
            
            // Pixabay API 要求 per_page 范围为 3-200
            int perPage = Math.max(3, Math.min(count * 2, 30));
            String url = String.format(
                    "%s?key=%s&q=%s&image_type=%s&orientation=%s&per_page=%d&safesearch=true&lang=zh",
                    PIXABAY_API_URL, pixabayApiKey, encodedQuery, imageType, orientation, perPage
            );
            
            // 日志中隐藏 API Key
            String logUrl = url.replace(pixabayApiKey, "***");
            log.info("Pixabay 请求URL: {}", logUrl);

            HttpResponse response = HttpRequest.get(url)
                    .timeout(15000)
                    .execute();

            log.info("Pixabay HTTP状态码: {}", response.getStatus());

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                int total = result.getInt("total", 0);
                JSONArray hits = result.getJSONArray("hits");
                
                log.info("Pixabay 返回结果: total={}, hits={}", total, hits != null ? hits.size() : 0);
                
                if (hits == null || hits.isEmpty()) {
                    log.warn("Pixabay 搜索无结果: query={}, total={}", query, total);
                    return null;
                }

                // 提取搜索关键词用于验证相关性
                Set<String> queryKeywords = extractKeywords(query);
                
                List<String> imageUrls = new ArrayList<>();
                String sizeKey = getPixabaySizeKeyByPurpose(purpose);
                
                // 遍历所有结果，优先选择标签匹配的图片
                for (int i = 0; i < hits.size() && imageUrls.size() < count; i++) {
                    JSONObject hit = hits.getJSONObject(i);
                    String tags = hit.getStr("tags", "").toLowerCase();
                    
                    // 检查图片标签是否与搜索关键词相关
                    boolean isRelevant = isImageRelevant(tags, queryKeywords);
                    
                    if (isRelevant) {
                        String imageUrl = hit.getStr(sizeKey);
                        if (imageUrl == null || imageUrl.isBlank()) {
                            imageUrl = hit.getStr("largeImageURL", hit.getStr("webformatURL"));
                        }
                        if (imageUrl != null && !imageUrl.isBlank()) {
                            log.debug("选中图片: tags={}, url={}", tags, imageUrl);
                            imageUrls.add(imageUrl);
                        }
                    } else {
                        log.debug("跳过不相关图片: tags={}", tags);
                    }
                }
                
                // 如果没有找到高相关性的图片，说明搜索词太特定（如品牌名）
                if (imageUrls.isEmpty()) {
                    log.warn("Pixabay 未找到与 \"{}\" 高度相关的图片（可能是品牌/特定产品名称）", query);
                    log.warn("建议使用更通用的关键词，如将 'xiaomi car su7' 改为 'electric car' 或 'sports car'");
                    return null;
                }

                String resultStr = String.join("\n", imageUrls);
                log.info("Pixabay 搜索成功，关键词: {}, 返回 {} 张相关图片", query, imageUrls.size());
                return "搜索到以下图片URL（来源: Pixabay）:\n" + resultStr;
            } else {
                log.error("Pixabay API 请求失败: {}", response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Pixabay 图片搜索异常", e);
            return null;
        }
    }

    /**
     * 从 Pixabay 按分类搜索图片
     */
    private String searchByCategoryFromPixabay(String category, int count, String purpose) {
        try {
            String orientation = getPixabayOrientationByPurpose(purpose);
            
            // Pixabay API 要求 per_page 范围为 3-200
            int perPage = Math.max(3, Math.min(count * 2, 30));
            String url = String.format(
                    "%s?key=%s&category=%s&orientation=%s&per_page=%d&safesearch=true&order=popular",
                    PIXABAY_API_URL, pixabayApiKey, category.toLowerCase(), orientation, perPage
            );

            HttpResponse response = HttpRequest.get(url)
                    .timeout(15000)
                    .execute();

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray hits = result.getJSONArray("hits");
                
                if (hits == null || hits.isEmpty()) {
                    log.info("Pixabay 分类未找到图片，分类: {}", category);
                    return null;
                }

                List<String> imageUrls = new ArrayList<>();
                String sizeKey = getPixabaySizeKeyByPurpose(purpose);
                
                for (int i = 0; i < Math.min(count, hits.size()); i++) {
                    JSONObject hit = hits.getJSONObject(i);
                    String imageUrl = hit.getStr(sizeKey);
                    if (imageUrl == null || imageUrl.isBlank()) {
                        imageUrl = hit.getStr("largeImageURL", hit.getStr("webformatURL"));
                    }
                    if (imageUrl != null && !imageUrl.isBlank()) {
                        imageUrls.add(imageUrl);
                    }
                }

                if (imageUrls.isEmpty()) {
                    return null;
                }

                String resultStr = String.join("\n", imageUrls);
                log.info("Pixabay 分类搜索成功，分类: {}, 返回 {} 张图片", category, imageUrls.size());
                return "搜索到以下图片URL（来源: Pixabay）:\n" + resultStr;
            } else {
                log.error("Pixabay API 请求失败: {}", response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Pixabay 分类搜索异常", e);
            return null;
        }
    }

    // ==================== Pexels 搜索实现 ====================

    /**
     * 从 Pexels 搜索图片（回退方案）
     */
    private String searchFromPexels(String query, int count, String purpose) {
        try {
            String orientation = getPexelsOrientationByPurpose(purpose);
            int perPage = Math.max(1, Math.min(count * 2, 15));
            
            log.info("Pexels 请求: query={}, per_page={}, orientation={}", query, perPage, orientation);
            
            HttpResponse response = HttpRequest.get(PEXELS_API_URL)
                    .header("Authorization", pexelsApiKey)
                    .form("query", query)
                    .form("per_page", perPage)
                    .form("orientation", orientation)
                    .timeout(10000)
                    .execute();

            log.info("Pexels HTTP状态码: {}", response.getStatus());

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray photos = result.getJSONArray("photos");
                
                if (photos == null || photos.isEmpty()) {
                    log.info("Pexels 未找到图片: {}", query);
                    return null;
                }

                List<String> imageUrls = new ArrayList<>();
                String sizeKey = getPexelsSizeKeyByPurpose(purpose);
                
                for (int i = 0; i < Math.min(count, photos.size()); i++) {
                    JSONObject photo = photos.getJSONObject(i);
                    JSONObject src = photo.getJSONObject("src");
                    String url = src.getStr(sizeKey, src.getStr("medium"));
                    if (url != null && !url.isBlank()) {
                        imageUrls.add(url);
                    }
                }

                if (imageUrls.isEmpty()) {
                    return null;
                }

                String resultStr = String.join("\n", imageUrls);
                log.info("Pexels 搜索成功（回退），关键词: {}, 返回 {} 张图片", query, imageUrls.size());
                return "搜索到以下图片URL（来源: Pexels）:\n" + resultStr;
            } else {
                log.error("Pexels API 请求失败: {}", response.body());
                return null;
            }
        } catch (Exception e) {
            log.error("Pexels 图片搜索异常", e);
            return null;
        }
    }

    // ==================== Pixabay 参数映射 ====================

    private String getPixabayImageTypeByPurpose(String purpose) {
        return "photo";
    }

    private String getPixabayOrientationByPurpose(String purpose) {
        return switch (purpose.toLowerCase()) {
            case "hero", "background" -> "horizontal";
            case "avatar" -> "all";
            case "card" -> "vertical";
            default -> "horizontal";
        };
    }

    private String getPixabaySizeKeyByPurpose(String purpose) {
        return switch (purpose.toLowerCase()) {
            case "hero", "background" -> "largeImageURL";
            case "avatar" -> "webformatURL";
            case "card" -> "webformatURL";
            default -> "largeImageURL";
        };
    }

    // ==================== Pexels 参数映射 ====================

    private String getPexelsOrientationByPurpose(String purpose) {
        return switch (purpose.toLowerCase()) {
            case "hero", "background" -> "landscape";
            case "avatar" -> "square";
            case "card" -> "portrait";
            default -> "landscape";
        };
    }

    private String getPexelsSizeKeyByPurpose(String purpose) {
        return switch (purpose.toLowerCase()) {
            case "hero", "background" -> "large2x";
            case "avatar" -> "small";
            case "card" -> "medium";
            default -> "large";
        };
    }

    // ==================== 占位图生成 ====================

    private String generatePlaceholderImages(String query, int count, String purpose) {
        List<String> urls = new ArrayList<>();
        int width = 800, height = 600;
        
        switch (purpose.toLowerCase()) {
            case "hero", "background" -> { width = 1920; height = 1080; }
            case "avatar" -> { width = 200; height = 200; }
            case "card" -> { width = 400; height = 300; }
        }

        for (int i = 0; i < count; i++) {
            String url = String.format("https://picsum.photos/%d/%d?random=%d", 
                    width, height, System.currentTimeMillis() + i);
            urls.add(url);
        }
        
        // 返回明确的失败信息，帮助 AI 正确处理搜索失败的情况
        return String.format(
                "[搜索失败] 未找到关键词 \"%s\" 的匹配图片，已使用随机占位图（建议告知用户搜索失败或换个关键词）:\n%s",
                query, String.join("\n", urls)
        );
    }

    // ==================== 搜索相关性验证方法 ====================

    /**
     * 从搜索查询中提取关键词
     * 排除常见的无意义词汇
     */
    private Set<String> extractKeywords(String query) {
        Set<String> keywords = new HashSet<>();
        // 常见的停用词（搜索时忽略）
        Set<String> stopWords = Set.of("the", "a", "an", "of", "in", "on", "for", "and", "or", "with");
        
        // 按空格和常见分隔符拆分
        String[] parts = query.toLowerCase().split("[\\s,_-]+");
        for (String part : parts) {
            String cleaned = part.trim();
            if (cleaned.length() >= 2 && !stopWords.contains(cleaned)) {
                keywords.add(cleaned);
            }
        }
        return keywords;
    }

    /**
     * 检查图片标签是否与搜索关键词相关
     * 要求至少匹配一个核心关键词
     */
    private boolean isImageRelevant(String tags, Set<String> queryKeywords) {
        if (tags == null || tags.isBlank() || queryKeywords.isEmpty()) {
            return false;
        }
        
        String tagsLower = tags.toLowerCase();
        int matchCount = 0;
        
        for (String keyword : queryKeywords) {
            // 检查标签是否包含关键词（支持部分匹配）
            if (tagsLower.contains(keyword)) {
                matchCount++;
            }
        }
        
        // 至少需要匹配 1 个关键词才认为相关
        // 对于多词搜索，提高阈值
        int requiredMatches = queryKeywords.size() >= 3 ? 2 : 1;
        return matchCount >= requiredMatches;
    }

    // ==================== BaseTool 抽象方法实现 ====================

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
