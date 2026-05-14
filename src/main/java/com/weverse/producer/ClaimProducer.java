package com.weverse.producer;

import com.weverse.dto.ClaimEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.claim}")
    private String claimTopic;

    public void publish(ClaimEvent event) {
        // orderId를 key로 사용해 동일 주문의 클레임이 항상 같은 파티션에 들어가도록 한다.
        // 순서 보장이 필요한 이유: 취소 → 재고 복구가 순서대로 처리되어야 하기 때문이다.
        kafkaTemplate.send(claimTopic, String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("클레임 이벤트 발행 실패 - orderId: {}, 원인: {}", event.getOrderId(), ex.getMessage(), ex);
                        return;
                    }
                    log.info("클레임 이벤트 발행 성공 - orderId: {}, topic: {}, partition: {}, offset: {}",
                            event.getOrderId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
