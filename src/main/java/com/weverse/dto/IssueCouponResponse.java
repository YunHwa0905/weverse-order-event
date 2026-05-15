package com.weverse.dto;

import com.weverse.entity.Coupon;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class IssueCouponResponse {

    private String couponCode;
    private int discountRate;
    private LocalDateTime expiredAt;

    public static IssueCouponResponse from(Coupon coupon) {
        return IssueCouponResponse.builder()
                .couponCode(coupon.getCouponCode())
                .discountRate(coupon.getDiscountRate())
                .expiredAt(coupon.getExpiredAt())
                .build();
    }
}
