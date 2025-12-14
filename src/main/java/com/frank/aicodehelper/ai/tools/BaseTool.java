package com.frank.aicodehelper.ai.tools;

import cn.hutool.json.JSONObject;
import com.frank.aicodehelper.constant.AppConstant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 工具基类
 * 定义所有工具的通用接口
 */
public abstract class BaseTool {

    /**
     * 项目目录前缀列表（按优先级排序）
     */
    private static final String[] PROJECT_DIR_PREFIXES = {
            "vue_project_",
            "html_",
            "multi_file_"
    };

    /**
     * 获取工具的英文名称（对应方法名）
     *
     * @return 工具英文名称
     */
    public abstract String getToolName();

    /**
     * 获取工具的中文显示名称
     *
     * @return 工具中文名称
     */
    public abstract String getDisplayName();

    /**
     * 生成工具请求时的返回值（显示给用户）
     *
     * @return 工具请求显示内容
     */
    public String generateToolRequestResponse() {
        return String.format("\n\n[选择工具] %s\n\n", getDisplayName());
    }

    /**
     * 生成工具执行结果格式（保存到数据库）
     *
     * @param arguments 工具执行参数
     * @return 格式化的工具执行结果
     */
    public abstract String generateToolExecutedResult(JSONObject arguments);

    /**
     * 根据 appId 自动检测并返回项目根目录
     * 按优先级检测 vue_project_、html_、multi_file_ 目录
     *
     * @param appId 应用 ID
     * @return 项目根目录路径，如果都不存在则返回 null
     */
    protected Path resolveProjectRoot(Long appId) {
        for (String prefix : PROJECT_DIR_PREFIXES) {
            String dirName = prefix + appId;
            Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, dirName);
            if (Files.exists(projectRoot) && Files.isDirectory(projectRoot)) {
                return projectRoot;
            }
        }
        return null;
    }

    /**
     * 解析文件的完整路径
     * 如果是相对路径，则自动检测项目根目录并拼接
     * 包含安全检查，防止路径遍历攻击
     *
     * @param relativeFilePath 相对文件路径
     * @param appId           应用 ID
     * @return 完整的文件路径
     * @throws SecurityException 如果路径不安全（绝对路径或路径遍历）
     */
    protected Path resolveFilePath(String relativeFilePath, Long appId) {
        //  安全检查1：禁止绝对路径
        Path path = Paths.get(relativeFilePath);
        if (path.isAbsolute()) {
            throw new SecurityException("安全限制：不允许使用绝对路径 - " + relativeFilePath);
        }

        //  安全检查2：禁止路径遍历序列
        if (relativeFilePath.contains("..")) {
            throw new SecurityException("安全限制：不允许使用路径遍历序列 - " + relativeFilePath);
        }

        Path projectRoot = resolveProjectRoot(appId);
        Path resolvedPath;
        if (projectRoot != null) {
            resolvedPath = projectRoot.resolve(relativeFilePath).normalize();
        } else {
            // 如果没有找到现有目录，默认使用 vue_project_ 前缀（用于新项目创建）
            String defaultDirName = "vue_project_" + appId;
            resolvedPath = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, defaultDirName)
                    .resolve(relativeFilePath).normalize();
        }

        //  安全检查3：确保解析后的路径仍在允许的根目录内
        Path allowedRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR).normalize();
        if (!resolvedPath.startsWith(allowedRoot)) {
            throw new SecurityException("安全限制：路径超出允许范围 - " + relativeFilePath);
        }

        return resolvedPath;
    }
}
