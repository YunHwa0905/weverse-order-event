package com.weverse.dto;

import com.weverse.entity.Order;
import com.weverse.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderDetailResponse {

    private Long orderId;
    private String memberId;
    private String productId;
    private int quantity;
    private long originalPrice;
    private long discountedPrice;
    private String couponCode;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderDetailResponse from(Order order) {
        return OrderDetailResponse.builder()
                .orderId(order.getOrderId())
                .memberId(order.getMemberId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .originalPrice(order.getOriginalPrice())
                .discountedPrice(order.getDiscountedPrice())
                .couponCode(order.getCouponCode())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
