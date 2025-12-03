package com.frank.aicodehelper.controller;


import com.frank.aicodehelper.constant.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/static")
public class StaticResourceController {

    // 应用生成根目录（用于浏览）
    private static final String PREVIEW_ROOT_DIR = AppConstant.CODE_OUTPUT_ROOT_DIR;

    /**
     * deployKey 白名单正则：只允许字母、数字、下划线、连字符
     */
    private static final String DEPLOY_KEY_PATTERN = "^[a-zA-Z0-9_-]+$";

    /**
     * 提供静态资源访问，支持目录重定向
     * 访问格式：http://localhost:8123/api/static/{deployKey}[/{fileName}]
     */
    @GetMapping("/{deployKey}/**")
    public ResponseEntity<Resource> serveStaticResource(
            @PathVariable String deployKey,
            HttpServletRequest request) {
        try {
            // 🔒 安全检查1：验证 deployKey 格式，防止路径遍历
            if (deployKey == null || !deployKey.matches(DEPLOY_KEY_PATTERN)) {
                log.warn("非法的 deployKey: {}", deployKey);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // 获取资源路径
            String resourcePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            resourcePath = resourcePath.substring(("/static/" + deployKey).length());
            // 如果是目录访问（不带斜杠），重定向到带斜杠的URL
            if (resourcePath.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", request.getRequestURI() + "/");
                return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
            }
            // 默认返回 index.html
            if (resourcePath.equals("/")) {
                resourcePath = "/index.html";
            }
            // 构建文件路径
            String filePath = PREVIEW_ROOT_DIR + "/" + deployKey + resourcePath;
            File file = new File(filePath);

            // 🔒 安全检查2：规范化路径后验证是否仍在允许目录内，防止路径遍历攻击
            String canonicalFilePath = file.getCanonicalPath();
            String canonicalRootPath = new File(PREVIEW_ROOT_DIR).getCanonicalPath();
            if (!canonicalFilePath.startsWith(canonicalRootPath + File.separator)) {
                log.warn("检测到路径遍历攻击: deployKey={}, resourcePath={}, 解析路径={}", 
                        deployKey, resourcePath, canonicalFilePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 检查文件是否存在
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            // 如果是目录，尝试返回目录下的 index.html
            if (file.isDirectory()) {
                File indexFile = new File(file, "index.html");
                if (indexFile.exists() && indexFile.isFile()) {
                    // 重定向到 index.html
                    HttpHeaders headers = new HttpHeaders();
                    String redirectUrl = request.getRequestURI();
                    if (!redirectUrl.endsWith("/")) {
                        redirectUrl += "/";
                    }
                    redirectUrl += "index.html";
                    headers.add("Location", redirectUrl);
                    return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
                }
                // 目录下没有 index.html
                return ResponseEntity.notFound().build();
            }
            // 返回文件资源
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .header("Content-Type", getContentTypeWithCharset(filePath))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文件扩展名返回带字符编码的 Content-Type
     */
    private String getContentTypeWithCharset(String filePath) {
        if (filePath.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filePath.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filePath.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg")) return "image/jpeg";
        return "application/octet-stream";
    }
}
