package com.frank.aicodehelper.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目下载服务接口
 *
 */
public interface ProjectDownloadService {

    /**
     * 将项目打包为ZIP并通过HTTP响应下载
     *
     * @param projectPath      项目路径
     * @param downloadFileName 下载文件名(不含.zip后缀)
     * @param response         HTTP响应对象
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}

