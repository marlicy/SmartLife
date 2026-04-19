package com.messaging;

import com.dto.SeckillOrderMessage;
import com.service.VoucherOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillOrderConsumer {

    @Resource
    private VoucherOrderService voucherOrderService;

    @KafkaListener(topics = "${smartlife.seckill.order-topic}", groupId = "smartlife-order-group")
    public void consume(SeckillOrderMessage message) {
        voucherOrderService.createVoucherOrder(message);
        log.info("seckill order created asynchronously, orderId={}", message.getOrderId());
    }
}
