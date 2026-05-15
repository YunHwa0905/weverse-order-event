package com.weverse.exception;

public class CouponNotFoundException extends BusinessException {

    public CouponNotFoundException(String couponCode) {
        super("쿠폰을 찾을 수 없습니다. couponCode: " + couponCode);
    }
}
