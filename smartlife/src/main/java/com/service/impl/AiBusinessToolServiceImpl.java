package com.service.impl;

import com.entity.Voucher;
import com.service.AiBusinessToolService;
import com.service.VoucherService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class AiBusinessToolServiceImpl implements AiBusinessToolService {

    private static final String RESERVATION_KEY_PREFIX = "ai:reservation:";

    @Resource
    private VoucherService voucherService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String summarizePlatform() {
        return "SmartLife 是一个本地生活服务平台，支持商铺查询、优惠券秒杀、智能客服咨询和基础运营能力。";
    }

    @Override
    public String lookupVoucher(Long voucherId) {
        if (voucherId == null) {
            return "请输入有效的优惠券ID。";
        }
        Voucher voucher = voucherService.getById(voucherId);
        if (voucher == null) {
            return "未查询到该优惠券。";
        }
        return "优惠券：" + voucher.getTitle() + "，门店ID：" + voucher.getShopId() + "，抵扣金额：" + voucher.getActualValue();
    }

    @Override
    public String createReservation(String sessionId, String shopName, String timeSlot, String note) {
        String reservationId = "RES-" + System.currentTimeMillis();
        String key = RESERVATION_KEY_PREFIX + sessionId + ":" + reservationId;
        String value = "{" +
                "\"reservationId\":\"" + reservationId + "\"," +
                "\"shopName\":\"" + shopName + "\"," +
                "\"timeSlot\":\"" + timeSlot + "\"," +
                "\"note\":\"" + note + "\"," +
                "\"createdAt\":\"" + LocalDateTime.now() + "\"" +
                "}";
        stringRedisTemplate.opsForValue().set(key, value, 24, TimeUnit.HOURS);
        return "预约已创建，预约号：" + reservationId + "，门店：" + shopName + "，时间：" + timeSlot;
    }
}
