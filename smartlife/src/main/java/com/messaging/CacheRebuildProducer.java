package com.messaging;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CacheRebuildProducer {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${smartlife.seckill.cache-rebuild-topic}")
    private String cacheRebuildTopic;

    public void sendDeleteCacheRetry(Long shopId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("shopId", shopId);
        kafkaTemplate.send(cacheRebuildTopic, String.valueOf(shopId), payload);
    }
}
