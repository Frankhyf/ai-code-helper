package com.frank.aicodehelper.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    /**
     * 代码生成类型：html(单文件) 或 multi_file(多文件)
     * 默认为 html
     */
    private String codeGenType;

    private static final long serialVersionUID = 1L;
}
