package com.weverse.consumer;

import com.weverse.dto.DlqPayload;
import com.weverse.dto.NotificationEvent;
import com.weverse.producer.NotificationProducer;
import com.weverse.service.ClaimService;
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
public class ClaimConsumer {

    private final ClaimService claimService;
    private final NotificationProducer notificationProducer;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.claim}")
    private String sourceTopic;

    @Value("${kafka.topics.order-dead-letter}")
    private String orderDeadLetterTopic;

    @KafkaListener(
            topics = "${kafka.topics.claim}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(Map<String, Object> payload, Acknowledgment ack) {
        Long orderId = ((Number) payload.get("orderId")).longValue()
                 ;

        try {
            claimService.processClaim(payload);

            notificationProducer.publish(NotificationEvent.builder()
                    .orderId(orderId)
                    .type(NotificationEvent.ORDER_CANCELLED)
                    .build());

            log.info("클레임 이벤트 처리 완료 - orderId: {}", orderId);
            ack.acknowledge();

        } catch (Exception ex) {
            log.error("클레임 이벤트 처리 실패 - orderId: {}, 원인: {}", orderId, ex.getMessage(), ex);
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
