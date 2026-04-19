package com.service.impl;

import cn.hutool.core.util.StrUtil;
import com.entity.AiToolCall;
import com.entity.AiToolResult;
import com.service.AiBusinessToolService;
import com.service.AiToolRouterService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class AiToolRouterServiceImpl implements AiToolRouterService {

    @Resource
    private AiBusinessToolService aiBusinessToolService;

    @Override
    public AiToolResult route(AiToolCall call) {
        if (call == null || StrUtil.isBlank(call.getToolName())) {
            return new AiToolResult("unknown", false, "工具调用参数为空");
        }
        String toolName = call.getToolName().toLowerCase(Locale.ROOT);
        Map<String, Object> args = call.getArguments();

        return switch (toolName) {
            case "platform_summary" -> new AiToolResult(call.getToolName(), true, aiBusinessToolService.summarizePlatform());
            case "voucher_lookup" -> new AiToolResult(call.getToolName(), true, aiBusinessToolService.lookupVoucher(toLong(args.get("voucherId"))));
            case "reservation_create" -> new AiToolResult(call.getToolName(), true, aiBusinessToolService.createReservation(
                    stringArg(args, "sessionId", "default-session"),
                    stringArg(args, "shopName", "默认门店"),
                    stringArg(args, "timeSlot", "待确认"),
                    stringArg(args, "note", "")
            ));
            default -> new AiToolResult(call.getToolName(), false, "未识别的工具：" + call.getToolName());
        };
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private String stringArg(Map<String, Object> args, String key, String defaultValue) {
        if (args == null || !args.containsKey(key) || args.get(key) == null) {
            return defaultValue;
        }
        String value = String.valueOf(args.get(key));
        return StrUtil.isBlank(value) ? defaultValue : value;
    }
}
