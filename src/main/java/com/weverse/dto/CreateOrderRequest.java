package com.weverse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CreateOrderRequest {

    @NotBlank
    private String memberId;

    @NotBlank
    private String productId;

    @Min(1)
    private int quantity;

    @Positive
    private long unitPrice;

    private String couponCode;
}
