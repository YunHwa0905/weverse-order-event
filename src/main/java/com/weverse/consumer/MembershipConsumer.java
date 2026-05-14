package com.weverse.consumer;

import com.weverse.dto.DlqPayload;
import com.weverse.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipConsumer {

    private final MembershipService membershipService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.membership}")
    private String sourceTopic;

    @Value("${kafka.topics.order-dead-letter}")
    private String orderDeadLetterTopic;

    @KafkaListener(
            topics = "${kafka.topics.membership}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(Map<String, Object> payload, Acknowledgment ack) {
        String memberId = (String) payload.get("memberId");

        try {
            membershipService.processMembership(payload);

            log.info("멤버십 이벤트 처리 완료 - memberId: {}", memberId);
            ack.acknowledge();

        } catch (Exception ex) {
            log.error("멤버십 이벤트 처리 실패 - memberId: {}, 원인: {}", memberId, ex.getMessage(), ex);
            kafkaTemplate.send(orderDeadLetterTopic, DlqPayload.builder()
                    .sourceTopic(sourceTopic)
                    .reason(ex.getMessage())
                    .failedAt(LocalDateTime.now().toString())
                    .originalPayload(payload)
                    .build());
            ack.acknowledge();
        }
    }
}
