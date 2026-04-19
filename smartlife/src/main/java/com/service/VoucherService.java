package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dto.Result;
import com.entity.Voucher;

public interface VoucherService extends IService<Voucher> {


    void addSeckillVoucher(Voucher voucher);
}
