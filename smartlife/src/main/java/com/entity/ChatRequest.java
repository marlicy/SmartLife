package com.entity;

import lombok.Data;

@Data
public class ChatRequest {
    private String sessionId;
    private String message;
}
