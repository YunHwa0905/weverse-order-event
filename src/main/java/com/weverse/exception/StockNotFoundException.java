package com.weverse.exception;

public class StockNotFoundException extends BusinessException {

    public StockNotFoundException(String productId) {
        super("재고 정보를 찾을 수 없습니다. productId: " + productId);
    }
}
