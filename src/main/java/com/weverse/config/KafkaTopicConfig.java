package com.weverse.weverseorderevent.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.order}")
    private String orderTopic;

    @Value("${kafka.topics.order-dead-letter}")
    private String orderDeadLetterTopic;

    @Value("${kafka.topics.order-notification}")
    private String orderNotificationTopic;

    @Value("${kafka.topics.claim}")
    private String claimTopic;

    @Value("${kafka.topics.membership}")
    private String membershipTopic;

    // docker-compose에서 KAFKA_AUTO_CREATE_TOPICS_ENABLE=false로 설정했기 때문에
    // 애플리케이션 기동 시 AdminClient를 통해 토픽을 직접 생성해야 한다.
    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers));
    }

    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name(orderTopic)
                .partitions(2)
                .replicas(1) // 단일 브로커 구성이므로 1 (운영 환경에서는 3 이상 권장)
                .build();
    }

    @Bean
    public NewTopic orderDeadLetterTopic() {
        return TopicBuilder.name(orderDeadLetterTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderNotificationTopic() {
        return TopicBuilder.name(orderNotificationTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic claimTopic() {
        return TopicBuilder.name(claimTopic)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic membershipTopic() {
        return TopicBuilder.name(membershipTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
