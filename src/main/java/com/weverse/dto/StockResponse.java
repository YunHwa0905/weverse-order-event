package com.weverse.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockResponse {

    private String productId;
    private int stock;
    private boolean cached;
}
