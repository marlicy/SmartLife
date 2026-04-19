package com.controller;

import com.dto.Result;
import com.entity.Voucher;
import com.service.VoucherService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequestMapping("/voucher")
public class VoucherController {
    @Resource
    private VoucherService voucherService;
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher){
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }
    @PostMapping("seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher){
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }


}
