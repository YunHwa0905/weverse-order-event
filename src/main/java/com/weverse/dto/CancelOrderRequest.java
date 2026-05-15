package com.weverse.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CancelOrderRequest {

    @NotBlank(message = "취소 사유를 입력해 주세요.")
    private String reason;
}
