package com.weverse.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationEvent {

    public static final String ORDER_COMPLETED = "ORDER_COMPLETED";
    public static final String ORDER_CANCELLED = "ORDER_CANCELLED";

    private Long orderId;
    private String memberId;
    private String type;
}
