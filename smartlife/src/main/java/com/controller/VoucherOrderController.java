package com.controller;

import com.annotation.RateLimiter;
import com.dto.Result;
import com.enums.RateLimitType;
import com.service.VoucherOrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private VoucherOrderService voucherOrderService;

    @PostMapping("/seckill/{id}")
    @RateLimiter(maxRequests = 20, windowSeconds = 5, type = RateLimitType.USER, key = "seckill")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }
}
