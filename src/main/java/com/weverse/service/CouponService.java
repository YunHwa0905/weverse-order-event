package com.weverse.service;

import com.weverse.dto.IssueCouponRequest;
import com.weverse.dto.IssueCouponResponse;
import com.weverse.entity.Coupon;
import com.weverse.exception.CouponNotFoundException;
import com.weverse.exception.InvalidCouponException;
import com.weverse.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private static final String REMAIN_KEY_PREFIX = "coupon:remain:";
    private static final String ISSUED_KEY_PREFIX = "coupon:issued:";

    private final CouponRepository couponRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public IssueCouponResponse issueCoupon(IssueCouponRequest request) {
        Coupon coupon = couponRepository.findByCouponCode(request.getCouponCode())
                .orElseThrow(() -> new CouponNotFoundException(request.getCouponCode()));

        if (coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCouponException("만료된 쿠폰입니다: " + request.getCouponCode());
        }

        String remainKey = REMAIN_KEY_PREFIX + request.getCouponCode();
        String issuedKey = ISSUED_KEY_PREFIX + request.getCouponCode();

        Boolean alreadyIssued = redisTemplate.opsForSet().isMember(issuedKey, request.getMemberId());
        if (Boolean.TRUE.equals(alreadyIssued)) {
            throw new InvalidCouponException("이미 발급된 쿠폰입니다: " + request.getCouponCode());
        }

        // 캐시 미스 시 DB 잔여 수량으로 초기화
        if (Boolean.FALSE.equals(redisTemplate.hasKey(remainKey))) {
            redisTemplate.opsForValue().set(remainKey, coupon.getRemainQuantity());
            log.warn("쿠폰 잔여 수량 캐시 미스 - couponCode: {}, DB 수량으로 초기화: {}",
                    request.getCouponCode(), coupon.getRemainQuantity());
        }

        Long remaining = redisTemplate.opsForValue().decrement(remainKey);
        if (remaining == null || remaining < 0) {
            redisTemplate.opsForValue().increment(remainKey); // 롤백
            throw new InvalidCouponException("소진된 쿠폰입니다: " + request.getCouponCode());
        }

        redisTemplate.opsForSet().add(issuedKey, request.getMemberId());

        coupon.decreaseRemainQuantity();

        log.info("쿠폰 발급 완료 - memberId: {}, couponCode: {}, 잔여: {}",
                request.getMemberId(), request.getCouponCode(), remaining);

        return IssueCouponResponse.from(coupon);
    }
}
