package com.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dto.Result;
import com.dto.SeckillOrderMessage;
import com.entity.VoucherOrder;

public interface VoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    void createVoucherOrder(SeckillOrderMessage message);

    void closeTimeoutOrders();
}
