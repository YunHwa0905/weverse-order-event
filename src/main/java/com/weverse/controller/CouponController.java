package com.weverse.controller;

import com.weverse.dto.IssueCouponRequest;
import com.weverse.dto.IssueCouponResponse;
import com.weverse.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public IssueCouponResponse issueCoupon(@RequestBody @Valid IssueCouponRequest request) {
        return couponService.issueCoupon(request);
    }
}
