``# weverse-order-event - CLAUDE.md

이 문서는 Claude(Code) 에이전트가
`weverse-order-event` 프로젝트에서 따라야 하는 개발 규칙과 컨벤션을 정의한다.

코드 생성 및 리팩터링 시 반드시 우선 참고한다.

---

# 1. 프로젝트 기본 정보

- Project: weverse-order-event
- Language: Java 21
- Framework: Spring Boot 3.2.5
- Build Tool: Gradle (Groovy DSL)

---

# 2. 기술 스택 규칙

## Backend
- Spring Boot 3.2.5 사용
- Java 21 문법 사용

## Database
- MySQL 8.0 사용
- ORM은 Spring Data JPA 사용

## Messaging
- Apache Kafka 사용
- KRaft 모드 사용
- Zookeeper 사용 금지

## Cache
- Redis 7.2 사용
- spring-data-redis 사용

---

# 3. 프로젝트 구조 규칙

루트 패키지:
`src/main/java/com/weverse`

## 패키지 구조

- controller
- service
- producer
- consumer
- entity
- repository
- dto
- config
- exception

새로운 패키지를 임의 생성하지 않는다.

---

# 4. 아키텍처 규칙

## 계층 구조

Controller
→ Service
→ Repository

구조를 반드시 유지한다.

Controller에서 Repository 직접 접근 금지.

---

# 5. DTO 규칙

## DTO / Entity 분리

- API 요청/응답은 DTO 사용
- Entity 직접 반환 금지

## DTO 위치
- dto 패키지 사용

---

# 6. Entity 규칙

## Entity 위치
- entity 패키지 사용

## JPA 규칙
- @Entity 사용
- 기본 생성자 보호 수준 유지
- 생성 시간/수정 시간 관리

---

# 7. Kafka 규칙

## Producer
- producer 패키지 사용

## Consumer
- consumer 패키지 사용

## Topic 이름
- order-topic
- order-dead-letter-topic
- order-notification-topic

하드코딩 대신 상수 또는 설정값 사용 권장.

---

# 8. Redis 규칙

## RedisTemplate 사용
- spring-data-redis 기반 구현

## 캐시 정책
- Cache Aside 패턴 유지

---

# 9. 예외 처리 규칙

## 전역 예외 처리
- @ControllerAdvice 사용

## 비즈니스 예외
- 커스텀 Exception 클래스 정의

예외 메시지는 명확하게 작성한다.

---

# 10. 로깅 규칙

## 로그 레벨

### INFO
- 정상 비즈니스 흐름

### WARN
- 캐시 미스
- 재시도 상황

### ERROR
- 예외 발생
- Kafka 처리 실패

## Logger
- Slf4j 사용

System.out.println 사용 금지.

---

# 11. Lombok 규칙

허용:
- @Getter
- @Setter
- @Builder
- @RequiredArgsConstructor

주의:
- @Data 남용 금지
- Entity에서 무분별한 @ToString 금지

---

# 12. 설정 파일 규칙

## application.yml
- 환경 설정 관리
- Kafka / Redis / DB 설정 포함

## docker-compose.yml
- Kafka
- MySQL
- Redis
- Kafdrop

실제 포트와 일치해야 한다.

---

# 13. 테스트 규칙

## 단위 테스트 작성 권장
- Service
- Producer
- Consumer

## 통합 테스트 작성 권장
- Kafka 이벤트 처리
- Redis 연동
- API 테스트

---

# 14. 코드 스타일 규칙

- Java 21 스타일 사용
- 의미 있는 변수명 사용
- 메서드는 단일 책임 원칙 유지
- 매직 넘버 사용 금지
- 중복 코드 최소화

---

# 15. 에이전트 행동 규칙

## 코드 생성 전 확인
다음 파일을 우선 확인한다:
- CLAUDE.md
- requirements.md
- application.yml
- docker-compose.yml

## 코드 생성 시
- 기존 패키지 구조 유지
- 기존 네이밍 컨벤션 유지
- 기존 아키텍처 유지

## 설정 변경 시
실제 Docker 환경과 설정값 일치 여부를 검증한다.

## 리팩터링 시
기존 기능 동작을 유지해야 한다.``