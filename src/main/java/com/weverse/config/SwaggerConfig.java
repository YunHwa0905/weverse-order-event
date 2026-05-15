package com.weverse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Weverse Order Event API")
                        .description("Kafka 기반 비동기 주문 이벤트 처리 시스템")
                        .version("1.0.0"));
    }
}
