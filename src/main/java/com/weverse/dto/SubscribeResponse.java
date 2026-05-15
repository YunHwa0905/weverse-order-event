package com.weverse.dto;

import com.weverse.entity.MembershipGrade;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscribeResponse {

    private String memberId;
    private MembershipGrade grade;
    private LocalDateTime subscribeEndAt;
}
