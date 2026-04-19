package com.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "smartlife.ai")
public class SmartlifeAiProperties {
    private boolean enabled;
    private String baseUrl;
    private String apiKey;
    private String model;
    private Integer memoryTtlMinutes = 30;
    private Integer maxMessages = 20;
    private Double temperature = 0.7;
}
