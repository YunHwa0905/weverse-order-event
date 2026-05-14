package com.weverse.weverseorderevent.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClaimEvent {

    private Long orderId;
    private String reason;
}
