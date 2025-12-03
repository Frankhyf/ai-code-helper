package com.frank.aicodehelper.service;

/**
 * 截图服务接口
 *
 * @author Frank
 */
public interface ScreenshotService {

    /**
     * 生成网页截图并上传到对象存储
     *
     * @param webUrl 网页URL
     * @return 截图的对象存储访问URL,失败返回null
     */
    String generateAndUploadScreenshot(String webUrl);
}

