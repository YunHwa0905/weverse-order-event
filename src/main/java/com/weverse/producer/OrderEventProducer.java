package com.weverse.producer;

import com.weverse.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order}")
    private String orderTopic;

    public void publish(OrderEvent event) {
        // key를 null로 지정해 Kafka 기본 파티셔너가 파티션 0, 1에 랜덤 분배하도록 한다.
        kafkaTemplate.send(orderTopic, null, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("주문 이벤트 발행 실패 - orderId: {}, 원인: {}", event.getOrderId(), ex.getMessage(), ex);
                        return;
                    }
                    log.info("주문 이벤트 발행 성공 - orderId: {}, topic: {}, partition: {}, offset: {}",
                            event.getOrderId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
