package com.weverse.exception;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException(String productId) {
        super("상품을 찾을 수 없습니다. productId: " + productId);
    }
}
