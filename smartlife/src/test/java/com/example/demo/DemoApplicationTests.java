package com.example.demo;

import com.entity.Shop;
import com.service.ShopService;
import com.utils.CacheClient;
import com.utils.RedisIdWorker;
import jakarta.annotation.Resource;
import jodd.cache.Cache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static com.utils.RedisConstants.*;



@SpringBootTest
class DemoApplicationTests {
    @Resource
    private ShopService shopService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisIdWorker redisIdWorker;
    private ExecutorService es = Executors.newFixedThreadPool(500);
    @Resource
    private CacheClient cacheClient;



    @Test
    void testSaveShop()throws Exception{
        Shop shop = new Shop();
        cacheClient.setWithLogicalExpire(CACHE_SHOP_KEY+1L,shop,10L, TimeUnit.MINUTES);
    }
    @Test
    void testIdWorker()throws InterruptedException{
        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () ->{
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id="+id);

            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time="+(end-begin));
    }


}
