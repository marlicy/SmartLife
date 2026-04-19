package com.messaging;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static com.utils.RedisConstants.CACHE_SHOP_KEY;

@Component
@Slf4j
public class CacheRebuildConsumer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @KafkaListener(topics = "${smartlife.seckill.cache-rebuild-topic}", groupId = "smartlife-cache-group")
    public void retryDeleteCache(Map<String, Object> payload) {
        Object rawShopId = payload.get("shopId");
        if (rawShopId == null) {
            return;
        }
        String redisKey = CACHE_SHOP_KEY + rawShopId;
        Boolean deleted = stringRedisTemplate.delete(redisKey);
        log.info("cache delete retry executed, key={}, result={}", redisKey, deleted);
    }
}
