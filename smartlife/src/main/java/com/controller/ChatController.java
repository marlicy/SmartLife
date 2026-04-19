package com.controller;

import com.annotation.RateLimiter;
import com.dto.Result;
import com.entity.ChatRequest;
import com.enums.RateLimitType;
import com.service.AiChatService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private AiChatService aiChatService;

    @PostMapping
    @RateLimiter(maxRequests = 10, windowSeconds = 60, type = RateLimitType.IP, key = "aiChat")
    public Result chat(@RequestBody ChatRequest request) {
        return Result.ok(aiChatService.chat(request));
    }
}
