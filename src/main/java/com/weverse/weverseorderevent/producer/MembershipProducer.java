package com.weverse.weverseorderevent.producer;

import com.weverse.weverseorderevent.dto.MembershipEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.membership}")
    private String membershipTopic;

    public void publish(MembershipEvent event) {
        // memberId를 key로 사용해 동일 회원의 멤버십 이벤트가 항상 같은 파티션으로 라우팅되도록 한다.
        kafkaTemplate.send(membershipTopic, event.getMemberId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("멤버십 이벤트 발행 실패 - memberId: {}, 원인: {}", event.getMemberId(), ex.getMessage(), ex);
                        return;
                    }
                    log.info("멤버십 이벤트 발행 성공 - memberId: {}, grade: {}, topic: {}, partition: {}, offset: {}",
                            event.getMemberId(),
                            event.getGrade(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
