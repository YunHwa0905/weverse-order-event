package com.weverse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId;

    @Column(nullable = false, unique = true)
    private String couponCode;

    @Column(nullable = false)
    private int discountRate;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int remainQuantity;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    // 잔여 수량이 0이 되면 true로 전환한다. Redis 원자적 감소와 별개로 DB 상태 확인용으로 사용한다.
    @Column(nullable = false)
    private boolean used;

    public void decreaseRemainQuantity() {
        this.remainQuantity--;
        if (this.remainQuantity == 0) {
            this.used = true;
        }
    }
}
