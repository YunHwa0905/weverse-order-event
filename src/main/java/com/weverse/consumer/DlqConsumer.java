package com.weverse.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class DlqConsumer {

    @KafkaListener(
            topics = "${kafka.topics.order-dead-letter}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(Map<String, Object> payload, Acknowledgment ack) {
        // DlqPayload 구조: sourceTopic, reason, failedAt, originalPayload
        // 각 컨슈머가 출처 정보를 포함해 전송하므로, 단일 DLQ에서도 어느 토픽에서 실패했는지 구분할 수 있다.
        String sourceTopic = (String) payload.get("sourceTopic");
        String reason = (String) payload.get("reason");
        String failedAt = (String) payload.get("failedAt");
        Object originalPayload = payload.get("originalPayload");

        // DLQ 메시지는 재처리하지 않고 기록만 한다.
        // 재처리 정책은 별도 운영 프로세스(수동 리드라이브 또는 알림)로 처리한다.
        log.error("DLQ 메시지 수신 - sourceTopic: {}, failedAt: {}, reason: {}, originalPayload: {}",
                sourceTopic, failedAt, reason, originalPayload);

        ack.acknowledge();
    }
}
