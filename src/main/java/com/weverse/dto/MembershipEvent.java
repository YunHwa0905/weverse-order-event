package com.weverse.weverseorderevent.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MembershipEvent {

    private String memberId;
    private String grade;
    private LocalDateTime subscribeStartAt;
    private LocalDateTime subscribeEndAt;
}
