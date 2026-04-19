package com.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.entity.AiToolCall;
import com.entity.AiToolResult;
import com.entity.ChatMessageRecord;
import com.entity.ChatRequest;
import com.config.SmartlifeAiProperties;
import com.service.AiChatService;
import com.service.AiToolRouterService;
import dev.langchain4j.model.openai.OpenAiLanguageModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private static final String CHAT_MEMORY_KEY_PREFIX = "chat:memory:";
    private static final String SYSTEM_PROMPT = "你是SmartLife平台的智能客服，负责解答商户信息、优惠券、预约到店、平台规则等问题。回答要简洁、专业，如果信息不足就明确说明。";
    private static final String TOOL_PROMPT = "当用户意图匹配工具时，请优先选择以下工具之一，并仅输出JSON：{" +
            "\"toolName\":\"platform_summary\"|\"voucher_lookup\"|\"reservation_create\",\"arguments\":{...}}。\n" +
            "若不需要工具，输出 plain text 回复。";

    @Resource
    private SmartlifeAiProperties aiProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private AiToolRouterService aiToolRouterService;

    @Override
    public String chat(ChatRequest request) {
        if (request == null || StrUtil.isBlank(request.getMessage())) {
            return "请输入有效的问题内容";
        }
        String sessionId = StrUtil.blankToDefault(request.getSessionId(), "default-session");
        String message = request.getMessage();

        AiToolCall toolCall = inferToolCall(sessionId, message);
        if (toolCall != null) {
            AiToolResult result = aiToolRouterService.route(toolCall);
            String content = result.isSuccess() ? result.getContent() : result.getContent();
            appendMessage(sessionId, new ChatMessageRecord("user", message));
            appendMessage(sessionId, new ChatMessageRecord("assistant", content));
            return content;
        }

        if (!aiProperties.isEnabled() || StrUtil.isBlank(aiProperties.getApiKey())) {
            String fallback = fallbackReply(message);
            appendMessage(sessionId, new ChatMessageRecord("user", message));
            appendMessage(sessionId, new ChatMessageRecord("assistant", fallback));
            return fallback;
        }

        List<ChatMessageRecord> history = loadHistory(sessionId);
        String prompt = buildPrompt(history, message);

        try {
            OpenAiLanguageModel model = OpenAiLanguageModel.builder()
                    .baseUrl(aiProperties.getBaseUrl())
                    .apiKey(aiProperties.getApiKey())
                    .modelName(aiProperties.getModel())
                    .temperature(aiProperties.getTemperature())
                    .timeout(Duration.ofSeconds(60))
                    .build();
            Response<String> response = model.generate(prompt);
            String reply = response.content();
            appendMessage(sessionId, new ChatMessageRecord("user", message));
            appendMessage(sessionId, new ChatMessageRecord("assistant", reply));
            return reply;
        } catch (Exception e) {
            log.error("AI chat request failed, sessionId={}", sessionId, e);
            String fallback = fallbackReply(message);
            appendMessage(sessionId, new ChatMessageRecord("user", message));
            appendMessage(sessionId, new ChatMessageRecord("assistant", fallback));
            return fallback;
        }
    }

    private AiToolCall inferToolCall(String sessionId, String message) {
        String lower = message.toLowerCase();
        if (lower.contains("介绍") || lower.contains("平台") || lower.contains("你是谁")) {
            return new AiToolCall("platform_summary", Map.of());
        }
        if (lower.contains("优惠券") && lower.matches(".*\\d+.*")) {
            Long voucherId = extractFirstLong(message);
            if (voucherId != null) {
                return new AiToolCall("voucher_lookup", Map.of("voucherId", voucherId));
            }
        }
        if (lower.contains("预约") || lower.contains("预定") || lower.contains("到店")) {
            return new AiToolCall("reservation_create", Map.of(
                    "sessionId", sessionId,
                    "shopName", "默认门店",
                    "timeSlot", "待确认",
                    "note", message
            ));
        }
        return null;
    }

    private Long extractFirstLong(String text) {
        StringBuilder number = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else if (number.length() > 0) {
                break;
            }
        }
        return number.length() == 0 ? null : Long.valueOf(number.toString());
    }

    private List<ChatMessageRecord> loadHistory(String sessionId) {
        String key = CHAT_MEMORY_KEY_PREFIX + sessionId;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return new ArrayList<>();
        }
        List<ChatMessageRecord> history = JSONUtil.toList(json, ChatMessageRecord.class);
        return history == null ? new ArrayList<>() : history;
    }

    private void appendMessage(String sessionId, ChatMessageRecord record) {
        List<ChatMessageRecord> history = loadHistory(sessionId);
        history.add(record);
        int maxSize = Math.max(aiProperties.getMaxMessages(), 2);
        if (history.size() > maxSize) {
            history = CollUtil.sub(history, history.size() - maxSize, history.size());
        }
        stringRedisTemplate.opsForValue().set(
                CHAT_MEMORY_KEY_PREFIX + sessionId,
                JSONUtil.toJsonStr(history),
                aiProperties.getMemoryTtlMinutes(),
                TimeUnit.MINUTES
        );
    }

    private String buildPrompt(List<ChatMessageRecord> history, String message) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(SYSTEM_PROMPT).append("\n").append(TOOL_PROMPT).append("\n\n");
        if (CollUtil.isNotEmpty(history)) {
            prompt.append("以下是历史对话：\n");
            for (ChatMessageRecord record : history) {
                String roleName = "assistant".equals(record.getRole()) ? "客服" : "用户";
                prompt.append(roleName).append(": ").append(record.getContent()).append("\n");
            }
        }
        prompt.append("用户: ").append(message).append("\n客服:");
        return prompt.toString();
    }

    private String fallbackReply(String message) {
        return "智能客服当前处于降级模式。你刚才的问题是：" + message + "。请先检查 smartlife.ai.enabled 和 API Key 配置，或稍后重试。";
    }
}
