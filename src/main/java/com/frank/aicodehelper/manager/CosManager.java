package com.frank.aicodehelper.manager;

import com.frank.aicodehelper.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * COS对象存储管理器
 *
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

     /**
     * 构建COS文件的访问URL
     *
     * @param key COS对象键
     * @return 完整的访问URL
     */
    private String buildFileUrl(String key) {
        String host = cosClientConfig.getHost();
        // 确保host不以/结尾，key不以/开头
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        return String.format("%s/%s", host, key);
    }

    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键(完整路径)
     * @param file 要上传的文件
     * @return 文件的访问URL,失败返回null
     */
    public String uploadFile(String key, File file) {
        // 上传文件
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            // 构建访问URL
            String url = buildFileUrl(key);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传COS失败,返回结果为空");
            return null;
        }
    }

    /**
     * 上传 MultipartFile 到 COS 并返回访问 URL
     *
     * @param key  COS对象键(完整路径)
     * @param multipartFile 要上传的文件
     * @return 文件的访问URL,失败返回null
     */
    public String uploadFile(String key, MultipartFile multipartFile) {
        try {
            InputStream inputStream = multipartFile.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    cosClientConfig.getBucket(),
                    key,
                    inputStream,
                    null
            );
            PutObjectResult result = cosClient.putObject(putObjectRequest);
            if (result != null) {
                // 构建访问URL
                String url = buildFileUrl(key);
                log.info("文件上传COS成功: {} -> {}", multipartFile.getOriginalFilename(), url);
                return url;
            } else {
                log.error("文件上传COS失败,返回结果为空");
                return null;
            }
        } catch (IOException e) {
            log.error("文件上传COS失败", e);
            return null;
        }
    }
}

