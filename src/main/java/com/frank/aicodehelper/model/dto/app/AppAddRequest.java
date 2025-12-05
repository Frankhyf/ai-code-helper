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
     * 代码生成类型：HTML、MULTI_FILE、VUE_PROJECT"
     */
    private String codeGenType;

    private static final long serialVersionUID = 1L;
}
