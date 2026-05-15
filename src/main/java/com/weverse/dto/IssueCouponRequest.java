package com.weverse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class IssueCouponRequest {

    @NotBlank(message = "memberId를 입력해 주세요.")
    private String memberId;

    @NotBlank(message = "couponCode를 입력해 주세요.")
    private String couponCode;
}
