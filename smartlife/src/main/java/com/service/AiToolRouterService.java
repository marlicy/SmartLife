package com.service;

import com.entity.AiToolCall;
import com.entity.AiToolResult;

public interface AiToolRouterService {
    AiToolResult route(AiToolCall call);
}
