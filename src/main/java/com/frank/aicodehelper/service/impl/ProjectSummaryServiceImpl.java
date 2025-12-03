package com.frank.aicodehelper.service.impl;

import cn.hutool.core.util.StrUtil;
import com.frank.aicodehelper.constant.AppConstant;
import com.frank.aicodehelper.model.enums.CodeGenTypeEnum;
import com.frank.aicodehelper.service.ProjectSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 项目状态摘要服务实现
 * 动态读取项目文件结构，生成摘要供 AI 参考
 * 
 * 性能考量：
 * - 文件树遍历深度限制为 4 层
 * - 每个目录最多显示 15 个文件
 * - 忽略 node_modules、.git 等大型目录
 * - 典型 Vue 项目（50-100个源文件）生成时间约 5-15ms
 *
 * @author Frank
 */
@Slf4j
@Service
public class ProjectSummaryServiceImpl implements ProjectSummaryService {

    /**
     * 需要忽略的目录列表（这些目录文件数量多且对 AI 无意义）
     */
    private static final Set<String> IGNORED_DIRS = Set.of(
            "node_modules",
            ".git",
            "dist",
            ".idea",
            ".vscode",
            "__pycache__",
            "target",
            "build",
            ".nuxt",
            ".next"
    );

    /**
     * 需要忽略的文件列表
     */
    private static final Set<String> IGNORED_FILES = Set.of(
            ".DS_Store",
            "package-lock.json",
            "yarn.lock",
            "pnpm-lock.yaml",
            ".gitignore"
    );

    /**
     * 文件树的最大深度（限制遍历深度以控制性能）
     */
    private static final int MAX_TREE_DEPTH = 4;

    /**
     * 单个目录下最大显示的文件数（避免文件过多导致摘要过长）
     */
    private static final int MAX_FILES_PER_DIR = 15;

    @Override
    public String generateProjectSummary(Long appId, CodeGenTypeEnum codeGenType) {
        long startTime = System.currentTimeMillis();
        
        // 构建项目路径
        String projectDirName = codeGenType.getValue() + "_" + appId;
        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + projectDirName;
        File projectDir = new File(projectPath);

        // 检查项目目录是否存在
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            log.debug("项目目录不存在，跳过摘要生成: {}", projectPath);
            return "";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("=== 当前项目状态 ===\n");
        summary.append("项目目录: ").append(projectDirName).append("\n\n");
        summary.append("文件结构:\n");
        summary.append(generateFileTree(projectDir, "", 0));

        // 添加最近修改的文件信息（帮助 AI 了解最近的改动）
        List<File> recentFiles = getRecentModifiedFiles(projectDir, 5);
        if (!recentFiles.isEmpty()) {
            summary.append("\n最近修改的文件:\n");
            for (File file : recentFiles) {
                String relativePath = getRelativePath(projectDir, file);
                summary.append("  - ").append(relativePath).append("\n");
            }
        }

        summary.append("\n=== 项目状态结束 ===\n\n");
        
        long duration = System.currentTimeMillis() - startTime;
        log.debug("项目状态摘要生成完成，appId: {}, 耗时: {}ms, 摘要长度: {} 字符", 
                appId, duration, summary.length());
        
        return summary.toString();
    }

    @Override
    public String enhanceUserMessage(String userMessage, Long appId, CodeGenTypeEnum codeGenType) {
        // 只对 VUE_PROJECT 类型注入项目状态（其他类型不涉及工具调用）
        if (codeGenType != CodeGenTypeEnum.VUE_PROJECT) {
            return userMessage;
        }

        String projectSummary = generateProjectSummary(appId, codeGenType);

        // 如果没有项目状态（首次创建），直接返回原始消息
        if (StrUtil.isBlank(projectSummary)) {
            return userMessage;
        }

        // 将项目状态和用户消息合并
        return projectSummary + "用户需求:\n" + userMessage;
    }

    /**
     * 生成文件树结构
     *
     * @param dir    目录
     * @param prefix 前缀（用于缩进）
     * @param depth  当前深度
     * @return 文件树字符串
     */
    private String generateFileTree(File dir, String prefix, int depth) {
        if (depth >= MAX_TREE_DEPTH) {
            return prefix + "└── ...\n";
        }

        StringBuilder tree = new StringBuilder();
        File[] files = dir.listFiles();

        if (files == null || files.length == 0) {
            return "";
        }

        // 过滤并排序：目录在前，文件在后，按名称排序
        List<File> filteredFiles = Arrays.stream(files)
                .filter(this::shouldInclude)
                .sorted(Comparator.comparing(File::isFile).thenComparing(File::getName))
                .limit(MAX_FILES_PER_DIR)
                .collect(Collectors.toList());

        int totalCount = (int) Arrays.stream(files).filter(this::shouldInclude).count();
        boolean hasMore = totalCount > MAX_FILES_PER_DIR;

        for (int i = 0; i < filteredFiles.size(); i++) {
            File file = filteredFiles.get(i);
            boolean isLast = (i == filteredFiles.size() - 1) && !hasMore;
            String connector = isLast ? "└── " : "├── ";
            String childPrefix = prefix + (isLast ? "    " : "│   ");

            if (file.isDirectory()) {
                tree.append(prefix).append(connector).append(file.getName()).append("/\n");
                tree.append(generateFileTree(file, childPrefix, depth + 1));
            } else {
                tree.append(prefix).append(connector).append(file.getName()).append("\n");
            }
        }

        // 如果有更多文件被省略
        if (hasMore) {
            int omitted = totalCount - MAX_FILES_PER_DIR;
            tree.append(prefix).append("└── ... (").append(omitted).append(" 个文件/目录省略)\n");
        }

        return tree.toString();
    }

    /**
     * 判断文件是否应该被包含在文件树中
     */
    private boolean shouldInclude(File file) {
        String name = file.getName();
        if (file.isDirectory()) {
            return !IGNORED_DIRS.contains(name);
        } else {
            return !IGNORED_FILES.contains(name);
        }
    }

    /**
     * 获取最近修改的文件列表
     *
     * @param dir   目录
     * @param limit 最大数量
     * @return 最近修改的文件列表
     */
    private List<File> getRecentModifiedFiles(File dir, int limit) {
        try (Stream<Path> paths = Files.walk(dir.toPath(), MAX_TREE_DEPTH)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> shouldInclude(path.toFile()))
                    .map(Path::toFile)
                    .sorted(Comparator.comparingLong(File::lastModified).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("获取最近修改文件失败: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 获取相对路径
     */
    private String getRelativePath(File baseDir, File file) {
        return baseDir.toPath().relativize(file.toPath()).toString().replace("\\", "/");
    }
}

