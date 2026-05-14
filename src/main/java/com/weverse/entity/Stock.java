package com.weverse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Stock {

    // 외부 시스템에서 관리하는 상품 ID를 PK로 사용하므로 @GeneratedValue를 사용하지 않는다.
    @Id
    private String productId;

    @Column(nullable = false)
    private int quantity;

    public void decrease(int amount) {
        this.quantity -= amount;
    }

    public void increase(int amount) {
        this.quantity += amount;
    }

    public boolean isAvailable(int requestedQuantity) {
        return this.quantity >= requestedQuantity;
    }
}
