package com.dto;

import lombok.Data;

@Data
public class SeckillOrderMessage {
    private Long orderId;
    private Long userId;
    private Long voucherId;
}
