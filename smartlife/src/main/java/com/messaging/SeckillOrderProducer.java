package com.messaging;

import com.dto.SeckillOrderMessage;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class SeckillOrderProducer {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${smartlife.seckill.order-topic}")
    private String orderTopic;

    public void send(SeckillOrderMessage message) {
        kafkaTemplate.send(orderTopic, String.valueOf(message.getOrderId()), message);
    }
}
