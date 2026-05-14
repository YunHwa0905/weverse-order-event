package com.weverse.consumer;

import com.weverse.dto.DlqPayload;
import com.weverse.dto.NotificationEvent;
import com.weverse.producer.NotificationProducer;
import com.weverse.service.OrderService;
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
public class OrderEventConsumer {

    private final OrderService orderService;
    private final NotificationProducer notificationProducer;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order}")
    private String sourceTopic;

    @Value("${kafka.topics.order-dead-letter}")
    private String orderDeadLetterTopic;

    @KafkaListener(
            topics = "${kafka.topics.order}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(Map<String, Object> payload, Acknowledgment ack) {
        // JSON 숫자는 크기에 따라 Integer 또는 Long으로 역직렬화되므로 Number로 받아 변환한다.
        Long orderId = ((Number) payload.get("orderId")).longValue();
        String memberId = (String) payload.get("memberId");

        try {
            orderService.processOrder(payload);

            notificationProducer.publish(NotificationEvent.builder()
                    .orderId(orderId)
                    .memberId(memberId)
                    .type(NotificationEvent.ORDER_COMPLETED)
                    .build());

            log.info("주문 이벤트 처리 완료 - orderId: {}", orderId);
            ack.acknowledge();

        } catch (Exception ex) {
            log.error("주문 이벤트 처리 실패 - orderId: {}, 원인: {}", orderId, ex.getMessage(), ex);
            kafkaTemplate.send(orderDeadLetterTopic, DlqPayload.builder()
                    .sourceTopic(sourceTopic)
                    .reason(ex.getMessage())
                    .failedAt(LocalDateTime.now().toString())
                    .originalPayload(payload)
                    .build());
            // 처리 실패 시에도 ack를 호출해 오프셋을 커밋한다.
            // ack 없이 종료하면 같은 메시지를 무한 재처리하므로, DLQ에 전달한 뒤 반드시 ack한다.
            ack.acknowledge();
        }
    }
}
