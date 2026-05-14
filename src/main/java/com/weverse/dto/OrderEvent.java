package com.weverse.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderEvent {

    private Long orderId;
    private String memberId;
    private String productId;
    private int quantity;
    private String couponCode;
    private long originalPrice;
    private long discountedPrice;
}
