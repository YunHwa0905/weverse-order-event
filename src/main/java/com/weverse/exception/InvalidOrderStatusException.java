package com.weverse.exception;

import com.weverse.entity.OrderStatus;

public class InvalidOrderStatusException extends BusinessException {

    public InvalidOrderStatusException(Long orderId, OrderStatus currentStatus) {
        super("취소 불가능한 주문 상태입니다. orderId: " + orderId + ", 현재 상태: " + currentStatus);
    }
}
