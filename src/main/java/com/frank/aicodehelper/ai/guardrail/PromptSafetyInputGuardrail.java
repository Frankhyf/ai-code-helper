package com.frank.aicodehelper.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class PromptSafetyInputGuardrail implements InputGuardrail {

    /**
     * 针对用户原始输入的最大长度限制（只校验用户输入，不包含 RAG 注入内容）
     */
    private static final int MAX_USER_INPUT_LENGTH = 1000;

    /**
     * 针对总输入的软限制（包含系统提示、RAG 注入等）。超过时仅记录，不拦截。
     */
    private static final int MAX_TOTAL_LENGTH_SOFT = 12000;

    // 敏感词列表
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "忽略之前的指令", "ignore previous instructions", "ignore above",
            "破解", "hack", "绕过", "bypass", "越狱", "jailbreak"
    );

    // 注入攻击模式
    private static final List<Pattern> INJECTION_PATTERNS = Arrays.asList(
            Pattern.compile("(?i)ignore\\s+(?:previous|above|all)\\s+(?:instructions?|commands?|prompts?)"),
            Pattern.compile("(?i)(?:forget|disregard)\\s+(?:everything|all)\\s+(?:above|before)"),
            Pattern.compile("(?i)(?:pretend|act|behave)\\s+(?:as|like)\\s+(?:if|you\\s+are)"),
            Pattern.compile("(?i)system\\s*:\\s*you\\s+are"),
            Pattern.compile("(?i)new\\s+(?:instructions?|commands?|prompts?)\\s*:")
    );

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String input = userMessage.singleText();

        // 1) 提取用户原始输入部分（忽略 RAG 或摘要注入）
        String userPart = extractUserPart(input);

        // 2) 长度校验：仅针对用户原始输入
        if (userPart.length() > MAX_USER_INPUT_LENGTH) {
            return fatal("输入内容过长，不要超过 " + MAX_USER_INPUT_LENGTH + " 字");
        }
        // 3) 空输入校验
        if (userPart.trim().isEmpty()) {
            return fatal("输入内容不能为空");
        }
        // 4) 敏感词校验（只针对用户原始输入）
        String lowerInput = userPart.toLowerCase();
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerInput.contains(sensitiveWord.toLowerCase())) {
                return fatal("输入包含不当内容，请修改后重试");
            }
        }
        // 5) 注入攻击模式校验（只针对用户原始输入）
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(userPart).find()) {
                return fatal("检测到恶意输入，请求被拒绝");
            }
        }

        // 6) 总长度软提示（不拦截，只记录）
        if (input.length() > MAX_TOTAL_LENGTH_SOFT) {
            // 仅返回成功，避免拦截，因为这部分包含 RAG/系统提示
            return success();
        }
        return success();
    }

    /**
     * 从增强后的消息中提取用户原始输入部分。
     * 兼容两种注入格式：
     * 1) RagEnhancedMessageService: "=== 用户需求 ===\n<userMessage>"
     * 2) ProjectSummaryService: "项目状态...\n用户需求:\n<userMessage>"
     */
    private String extractUserPart(String input) {
        if (input == null) {
            return "";
        }
        String marker1 = "=== 用户需求 ===";
        String marker2 = "用户需求:\n";

        int idx = input.indexOf(marker1);
        if (idx >= 0) {
            return input.substring(idx + marker1.length()).trim();
        }
        idx = input.indexOf(marker2);
        if (idx >= 0) {
            return input.substring(idx + marker2.length()).trim();
        }
        // 默认返回原始文本
        return input;
    }
}

