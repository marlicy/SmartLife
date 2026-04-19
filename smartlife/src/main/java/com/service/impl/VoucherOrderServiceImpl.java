package com.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dto.Result;
import com.dto.SeckillOrderMessage;
import com.entity.SeckillVoucher;
import com.entity.VoucherOrder;
import com.mapper.VoucherOrderMapper;
import com.messaging.SeckillOrderProducer;
import com.service.SeckillVoucherService;
import com.service.VoucherOrderService;
import com.utils.RedisIdWorker;
import com.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements VoucherOrderService {

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Resource
    private SeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SeckillOrderProducer seckillOrderProducer;

    @Override
    public Result seckillVoucher(Long voucherId) {
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        if (voucher == null) {
            return Result.fail("优惠券不存在");
        }
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始！");
        }
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束！");
        }

        Long userId = UserHolder.getUser().getId();
        Long scriptResult = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );
        if (scriptResult == null) {
            return Result.fail("秒杀请求失败");
        }
        if (scriptResult == 1L) {
            return Result.fail("库存不足！");
        }
        if (scriptResult == 2L) {
            return Result.fail("同一用户不可重复下单！");
        }

        long orderId = redisIdWorker.nextId("order");
        SeckillOrderMessage message = new SeckillOrderMessage();
        message.setOrderId(orderId);
        message.setUserId(userId);
        message.setVoucherId(voucherId);
        seckillOrderProducer.send(message);
        return Result.ok(orderId);
    }

    @Override
    @Transactional
    public void createVoucherOrder(SeckillOrderMessage message) {
        RLock lock = redissonClient.getLock("lock:order:" + message.getUserId());
        boolean locked = lock.tryLock();
        if (!locked) {
            log.warn("duplicate order request, userId={}", message.getUserId());
            return;
        }
        try {
            long count = lambdaQuery()
                    .eq(VoucherOrder::getUserId, message.getUserId())
                    .eq(VoucherOrder::getVoucherId, message.getVoucherId())
                    .count();
            if (count > 0) {
                log.warn("user already ordered, userId={}, voucherId={}", message.getUserId(), message.getVoucherId());
                return;
            }

            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", message.getVoucherId())
                    .gt("stock", 0)
                    .update();
            if (!success) {
                log.warn("deduct stock failed, voucherId={}", message.getVoucherId());
                return;
            }

            VoucherOrder voucherOrder = new VoucherOrder();
            voucherOrder.setId(message.getOrderId());
            voucherOrder.setUserId(message.getUserId());
            voucherOrder.setVoucherId(message.getVoucherId());
            voucherOrder.setStatus(1);
            save(voucherOrder);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Scheduled(cron = "0 */1 * * * ?")
    public void closeTimeoutOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);
        List<VoucherOrder> timeoutOrders = lambdaQuery()
                .eq(VoucherOrder::getStatus, 1)
                .lt(VoucherOrder::getCreateTime, expireTime)
                .list();
        for (VoucherOrder order : timeoutOrders) {
            boolean updated = lambdaUpdate()
                    .eq(VoucherOrder::getId, order.getId())
                    .eq(VoucherOrder::getStatus, 1)
                    .set(VoucherOrder::getStatus, 4)
                    .update();
            if (updated) {
                seckillVoucherService.update()
                        .setSql("stock = stock + 1")
                        .eq("voucher_id", order.getVoucherId())
                        .update();
                log.info("closed timeout order, orderId={}", order.getId());
            }
        }
    }
}
