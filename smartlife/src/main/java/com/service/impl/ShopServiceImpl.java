package com.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dto.Result;
import com.entity.Shop;
import com.mapper.ShopMapper;
import com.messaging.CacheRebuildProducer;
import com.service.ShopService;
import com.utils.CacheClient;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.utils.RedisConstants.CACHE_SHOP_TTL;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private CacheRebuildProducer cacheRebuildProducer;

    @Override
    public Result queryShopById(Long id) {
        Shop shop = queryShopCache(id);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    @Override
    @Cacheable(cacheNames = "shopCache", key = "#id")
    public Shop queryShopCache(Long id) {
        return cacheClient.queryWithPassThrough(
                CACHE_SHOP_KEY,
                id,
                Shop.class,
                this::getById,
                CACHE_SHOP_TTL,
                TimeUnit.MINUTES
        );
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "shopCache", key = "#shop.id")
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        boolean success = updateById(shop);
        if (!success) {
            return Result.fail("店铺不存在");
        }
        String redisKey = CACHE_SHOP_KEY + id;
        try {
            Boolean deleted = stringRedisTemplate.delete(redisKey);
            if (Boolean.FALSE.equals(deleted)) {
                cacheRebuildProducer.sendDeleteCacheRetry(id);
            }
        } catch (Exception e) {
            cacheRebuildProducer.sendDeleteCacheRetry(id);
        }
        return Result.ok();
    }
}
