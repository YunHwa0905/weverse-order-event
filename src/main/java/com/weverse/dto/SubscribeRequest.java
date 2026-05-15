package com.weverse.dto;

import com.weverse.entity.MembershipGrade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SubscribeRequest {

    @NotBlank(message = "memberId를 입력해 주세요.")
    private String memberId;

    @NotNull(message = "구독 등급을 입력해 주세요.")
    private MembershipGrade grade;

    private String artistId;
}
