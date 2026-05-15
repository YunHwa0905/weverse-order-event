package com.weverse.exception;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productId) {
        super("재고가 부족합니다. productId: " + productId);
    }
}
