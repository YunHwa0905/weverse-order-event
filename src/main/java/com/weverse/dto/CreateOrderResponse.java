package com.weverse.dto;

import com.weverse.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateOrderResponse {

    private Long orderId;
    private OrderStatus status;
    private long originalPrice;
    private long discountedPrice;
}
