package com.weverse.producer;

import com.weverse.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order-notification}")
    private String orderNotificationTopic;

    public void publish(NotificationEvent event) {
        // orderId를 key로 사용해 동일 주문의 알림이 항상 같은 파티션으로 라우팅되도록 한다.
        kafkaTemplate.send(orderNotificationTopic, String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("알림 이벤트 발행 실패 - orderId: {}, type: {}, 원인: {}",
                                event.getOrderId(), event.getType(), ex.getMessage(), ex);
                        return;
                    }
                    log.info("알림 이벤트 발행 성공 - orderId: {}, type: {}, partition: {}, offset: {}",
                            event.getOrderId(),
                            event.getType(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
