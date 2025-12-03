package com.frank.aicodehelper.ai.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Pixabay API 连通性测试
 */
public class PixabayImageToolTest {

    // 你的 Pixabay API Key
    private static final String API_KEY = "53524828-c30ead37aaa02f56d25c36b66";
    private static final String PIXABAY_API_URL = "https://pixabay.com/api/";

    @Test
    public void testPixabayApiConnection() {
        System.out.println("========== Pixabay API 连通性测试 ==========\n");

        // 测试1: 英文关键词搜索
        System.out.println("【测试1】英文关键词搜索: business office");
        testSearch("business office", 3, "horizontal");

        // 测试2: 中文关键词搜索
        System.out.println("\n【测试2】中文关键词搜索: 商务办公");
        testSearch("商务办公", 3, "horizontal");

        // 测试3: 分类搜索
        System.out.println("\n【测试3】分类搜索: nature");
        testSearchByCategory("nature", 3);

        // 测试4: 矢量图搜索
        System.out.println("\n【测试4】矢量图搜索: icon");
        testSearchVector("icon", 3);

        System.out.println("\n========== 测试完成 ==========");
    }

    private void testSearch(String query, int count, String orientation) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "%s?key=%s&q=%s&image_type=photo&orientation=%s&per_page=%d&safesearch=true&lang=zh",
                    PIXABAY_API_URL, API_KEY, encodedQuery, orientation, count
            );

            System.out.println("请求URL: " + url.replace(API_KEY, "***"));

            HttpResponse response = HttpRequest.get(url)
                    .timeout(15000)
                    .execute();

            System.out.println("HTTP状态码: " + response.getStatus());

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                int total = result.getInt("total", 0);
                int totalHits = result.getInt("totalHits", 0);
                JSONArray hits = result.getJSONArray("hits");

                System.out.println("总结果数: " + total);
                System.out.println("返回数量: " + totalHits);
                System.out.println("实际返回: " + (hits != null ? hits.size() : 0) + " 张图片");

                if (hits != null && !hits.isEmpty()) {
                    System.out.println("图片URL列表:");
                    for (int i = 0; i < Math.min(count, hits.size()); i++) {
                        JSONObject hit = hits.getJSONObject(i);
                        String imageUrl = hit.getStr("largeImageURL", hit.getStr("webformatURL"));
                        System.out.println("  [" + (i + 1) + "] " + imageUrl);
                    }
                    System.out.println("✅ 测试通过");
                } else {
                    System.out.println("⚠️ 未找到图片");
                }
            } else {
                System.out.println("❌ 请求失败: " + response.body());
            }
        } catch (Exception e) {
            System.out.println("❌ 异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testSearchByCategory(String category, int count) {
        try {
            String url = String.format(
                    "%s?key=%s&category=%s&per_page=%d&safesearch=true&order=popular",
                    PIXABAY_API_URL, API_KEY, category, count
            );

            System.out.println("请求URL: " + url.replace(API_KEY, "***"));

            HttpResponse response = HttpRequest.get(url)
                    .timeout(15000)
                    .execute();

            System.out.println("HTTP状态码: " + response.getStatus());

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray hits = result.getJSONArray("hits");

                System.out.println("实际返回: " + (hits != null ? hits.size() : 0) + " 张图片");

                if (hits != null && !hits.isEmpty()) {
                    System.out.println("图片URL列表:");
                    for (int i = 0; i < Math.min(count, hits.size()); i++) {
                        JSONObject hit = hits.getJSONObject(i);
                        String imageUrl = hit.getStr("largeImageURL");
                        System.out.println("  [" + (i + 1) + "] " + imageUrl);
                    }
                    System.out.println("✅ 测试通过");
                } else {
                    System.out.println("⚠️ 未找到图片");
                }
            } else {
                System.out.println("❌ 请求失败: " + response.body());
            }
        } catch (Exception e) {
            System.out.println("❌ 异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testSearchVector(String query, int count) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(
                    "%s?key=%s&q=%s&image_type=vector&per_page=%d&safesearch=true",
                    PIXABAY_API_URL, API_KEY, encodedQuery, count
            );

            System.out.println("请求URL: " + url.replace(API_KEY, "***"));

            HttpResponse response = HttpRequest.get(url)
                    .timeout(15000)
                    .execute();

            System.out.println("HTTP状态码: " + response.getStatus());

            if (response.isOk()) {
                JSONObject result = JSONUtil.parseObj(response.body());
                JSONArray hits = result.getJSONArray("hits");

                System.out.println("实际返回: " + (hits != null ? hits.size() : 0) + " 张矢量图");

                if (hits != null && !hits.isEmpty()) {
                    System.out.println("矢量图URL列表:");
                    for (int i = 0; i < Math.min(count, hits.size()); i++) {
                        JSONObject hit = hits.getJSONObject(i);
                        String imageUrl = hit.getStr("largeImageURL");
                        System.out.println("  [" + (i + 1) + "] " + imageUrl);
                    }
                    System.out.println("✅ 测试通过");
                } else {
                    System.out.println("⚠️ 未找到矢量图");
                }
            } else {
                System.out.println("❌ 请求失败: " + response.body());
            }
        } catch (Exception e) {
            System.out.println("❌ 异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

