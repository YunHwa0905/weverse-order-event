package com.weverse.dto;

import com.weverse.entity.Member;
import com.weverse.entity.MembershipGrade;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberStatusResponse {

    private String memberId;
    private MembershipGrade grade;
    private LocalDateTime subscribeEndAt;

    public static MemberStatusResponse from(Member member) {
        return MemberStatusResponse.builder()
                .memberId(member.getMemberId())
                .grade(member.getMembershipGrade())
                .subscribeEndAt(member.getSubscribeEndAt())
                .build();
    }
}
