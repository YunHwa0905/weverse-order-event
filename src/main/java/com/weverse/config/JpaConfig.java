package com.weverse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @CreatedDate, @LastModifiedDate가 동작하려면 JPA Auditing을 활성화해야 한다.
// Application 클래스에 두면 테스트 슬라이스(@DataJpaTest 등)에서 컨텍스트 로드 오류가 생기므로 별도 Config로 분리한다.
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
