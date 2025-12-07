package com.frank.aicodehelper.rag.chunking;

import com.frank.aicodehelper.rag.model.CodeContextDocument;

import java.util.List;

/**
 * 代码分块策略接口
 * 不同项目类型使用不同的分块逻辑
 */
public interface ChunkingStrategy {

    /**
     * 将代码文件分块
     *
     * @param appId    应用ID
     * @param filePath 文件路径
     * @param content  文件内容
     * @return 分块后的文档列表
     */
    List<CodeContextDocument> chunk(Long appId, String filePath, String content);

    /**
     * 获取支持的文件扩展名
     */
    List<String> getSupportedExtensions();

    /**
     * 判断是否支持该文件
     *
     * @param filePath 文件路径
     * @return 是否支持
     */
    default boolean supports(String filePath) {
        return getSupportedExtensions().stream()
                .anyMatch(ext -> filePath.toLowerCase().endsWith(ext));
    }
}


