package com.weverse.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DlqPayload {

    private String sourceTopic;   // 실패가 발생한 원본 토픽
    private String reason;        // 예외 메시지
    private String failedAt;      // 실패 시각 (ISO-8601)
    private Map<String, Object> originalPayload; // 처리 실패한 원본 메시지
}
