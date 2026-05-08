weverse-order-event - CLAUDE.md

이 파일은 `weverse-order-event` 프로젝트에서 Claude(Code) 에이전트가 따르는 개발 가이드, 규칙, 및 컨벤션이다.  
코드를 생성하거나 리팩터링할 때는 이 문서를 우선 참고해야 한다.

--------------------------------------------------------------------------------

1. 프로젝트 개요

- 프로젝트명: `weverse-order-event`
- 목적: 멤버십/커머스 도메인에서 Kafka 기반 주문 이벤트 처리, Redis 캐싱, JPA 기반 영속성을 실무 수준으로 구현하는 백엔드 실습 프로젝트.
- 도메인: 주문, 재고, 알림 이벤트 처리
- 도메인 개념:
    - 주문(Order)은 상태(PENDING, PROCESSING, COMPLETED, FAILED)를 가지며,
    - 주문 생성 이벤트가 Kafka Topic을 통해 처리되고,
    - 재고 정보는 Redis 캐시를 우선 사용하며, 미스 시 MySQL을 조회한다.
- GitHub: 프로젝트 완료 후 별도 저장소에 생성할 예정.

2. 기술 스택

- Backend: Java 21, Spring Boot 3.2.5
- Messaging: Apache Kafka 7.7.0 (KRaft 모드, Zookeeper 없음)
- Cache: Redis 7.2
- Database: MySQL 8.0 (JPA)
- Infra: Docker Compose
- Monitoring: Kafdrop (Kafka 모니터링)
- API: REST API

3. 개발 환경

- OS: Windows 11
- IDE: IntelliJ IDEA 2024.3.3
- JDK: Eclipse Temurin 21 (LTS)
- Build Tool: Gradle (Groovy DSL)
- DB GUI: DBeaver Community

4. 프로젝트 기본 정보

- 빌드 시스템: Gradle (Groovy)
- Group: `com.weverse`
- Artifact: `weverse-order-event`
- Packaging: `jar`
- Java 버전: 21
- 프로젝트 구조는 `src/main/java/com/weverse` 아래에 패키지를 구성한다.

5. 주요 의존성 및 용도

- `spring-boot-starter-web`
    - REST API 엔드포인트 개발
- `spring-kafka`
    - Kafka Producer/Consumer 구현
- `spring-data-jpa`
    - MySQL ORM 및 Entity/Repository 관리
- `spring-data-redis`
    - Redis 재고 캐싱, Redis Template 사용
- `mysql-connector-java`
    - MySQL 데이터베이스 연결
- `lombok`
    - Getter, Setter, Builder, RequiredArgsConstructor 등 보일러플레이트 코드 제거
- `spring-boot-starter-validation`
    - 요청 DTO 유효성 검사(@Valid, @NotBlank 등) 사용

6. 인프라 구성 (Docker Compose)

- `kafka`
    - 이미지: `confluentinc/cp-kafka:7.7.0`
    - 포트: `9092`
    - KRaft 모드로 실행되며, Zookeeper는 사용하지 않는다.
- `mysql`
    - 이미지: `mysql:8.0`
    - 포트: `3306`
    - 데이터베이스명: `weverse_order`
    - root 비밀번호: `root1234`
- `redis`
    - 이미지: `redis:7.2`
    - 포트: `6379`
- `kafdrop`
    - 이미지: `obsidiandynamics/kafdrop:latest`
    - 포트: `9000`
    - Kafka 브로커 연결: `kafka:9092`
    - 모니터링용으로, Topic, Partition, Consumer Group, Offset를 확인한다.

실행 명령어:
- 인프라 전체 시작: `docker-compose up -d`
- 상태 확인: `docker-compose ps`
- 전체 종료: `docker-compose down`

7. 애플리케이션 설정 (application.yml)

- 데이터베이스
    - URL: `jdbc:mysql://localhost:3306/weverse_order?serverTimezone=UTC&characterEncoding=UTF-8`
    - username: `root`
    - password: `root1234`
    - JPA: `hibernate.ddl-auto: update`, `show-sql: true` 활성화

- Kafka
    - bootstrap-servers: `localhost:9092`
    - Consumer Group: `weverse-order-group`
    - auto-offset-reset: `earliest`
    - key/value deserializer/serializer는 Json 기반으로 설정

- Redis
    - host: `localhost`
    - port: `6379`

- 서버
    - 포트: `8080`

8. 디렉토리 구조 및 패키징

루트 경로: `weverse-order-event/`

- `docker-compose.yml`
    - 인프라 구성 파일
- `src/main/java/com/weverse/`
    - `controller/` : REST API 엔드포인트
    - `service/` : 비즈니스 로직
    - `producer/` : Kafka Producer 구현
    - `consumer/` : Kafka Consumer 구현
    - `entity/` : JPA Entity
    - `repository/` : JPA Repository
    - `config/` : Kafka, Redis configuration
    - `dto/` : 요청/응답용 DTO
- `src/main/resources/application.yml`
    - 프로필별 설정은 향후 필요 시 분리
- `README.md`
    - 프로젝트 설명, 실행 방법, API 명세, Kafdrop 확인 방법 등 정리

9. Kafka 기본 설정 및 토픽

Kafka는 KRaft 모드로 실행되며, Zookeeper는 포함하지 않는다.  
주요 토픽:

- `order-topic`
    - 주문 생성 이벤트를 발행/수신
    - 파티션 2개로 구성
- `order-dead-letter-topic`
    - Consumer 처리 실패 시 이벤트 전달
    - 실패 원인은 로그를 통해 기록
- `order-notification-topic`
    - 주문 완료 알림 이벤트 발행

Kafka Consumer Group:
- `weverse-order-group`
    - `order-topic`을 구독하며, `earliest` offset으로 시작

10. 도메인 및 엔티티 설계

- `Order` (주문)
    - `orderId`: Long (PK)
    - `memberId`: String
    - `productId`: String
    - `quantity`: Integer
    - `status`: Enum
        - `PENDING` → `PROCESSING` → `COMPLETED` / `FAILED`
    - `createdAt`: LocalDateTime
    - `updatedAt`: LocalDateTime

- Kafka 이벤트 흐름
    1. `POST /api/orders` 요청 수신
    2. `OrderProducer`가 `order-topic`에 JSON 형식으로 이벤트 발행 (파티션 0, 1 랜덤 분배)
    3. `OrderConsumer`가 `order-topic` 구독
        - 성공 시: MySQL에 Order 저장, Redis 재고 차감, `order-notification-topic` 발행
        - 실패 시: `order-dead-letter-topic`에 이벤트 전달, 로그 기록

11. Redis 캐시 정책

- 재고 조회
    1. 우선 Redis에서 재고 조회
    2. 미스 시 MySQL 조회 후 결과를 Redis에 저장 (캐시 업데이트)
- 재고 변경
    - 주문 성공 시 Redis 재고 감소
    - 필요 시 Periodic 배치나 TTL 기반으로 캐시 무효화/재동기화 검토
- Redis는 `localhost:6379`에서 접근하며, `spring-data-redis` 사용

12. API 명세 (요약)

- `POST /api/orders` — 주문 생성
    - 요청: `{ "memberId": "...", "productId": "...", "quantity": 2 }`
    - 응답: `{ "orderId": 1, "status": "PENDING", ... }`

- `GET /api/orders/{orderId}` — 주문 조회
    - 응답: Order 객체

- `GET /api/orders/stock/{productId}` — 재고 조회
    - 응답: `{ "productId": "...", "stock": 98, "cached": true }`
    - `cached` 필드는 Redis에서 조회했는지 여부를 나타냄

13. 개발 및 구현 순서 (Guideline)

1. Docker Compose 인프라 세팅
2. Spring Boot 프로젝트 생성
3. Kafka Config 및 Topic 정의
4. Producer 구현
5. Consumer 구현
6. JPA Entity 및 Repository 설계
7. Redis 캐싱 로직 구현
8. DLQ(Dead Letter Topic) 처리 로직
9. REST API 컨트롤러 구현
10. Kafdrop 모니터링 확인 및 운영성 관련 로깅/에러 처리
11. GitHub 문서 및 README 작성

이 순서를 따라 구현하되, 각 단계가 완료되면 단위 테스트와 로컬 통합 테스트를 수행한다.

14. 코드 스타일 및 컨벤션

- Java 21 기준 최신 스타일 사용
- Spring Boot 3.2.5 가이드에 맞는 스타일 유지
- Lombok 사용 시
    - `@Data`는 필드가 많지 않은 경우에만 사용
    - `@EqualsAndHashCode` 및 `@ToString`은 필요 시 명시적으로 설정
- DTO/Entity 분리
    - API 요청·응답 DTO는 `dto` 패키지에,
    - DB Entity는 `entity` 패키지에 두고 명확히 구분
- 로깅
    - 비즈니스 로그는 `INFO` 또는 `WARN`
    - 오류 및 예외는 `ERROR`로 기록
- 예외 처리
    - 비즈니스 예외는 별도 `Exception` 클래스 정의
    - `@ControllerAdvice`를 사용해 전역 예외 처리

15. 에이전트 사용 가이드

- 새로운 기능 추가 전
    - 먼저 `weverse-order-event` 디렉토리에 포함된 `docker-compose.yml`, `application.yml`, `CLAUDE.md` 내용을 확인한다.
- 코드 생성 시
    - 기존 패키지 구조(예: `controller`, `service`, `producer`, `consumer`, `dto`, `entity`, `repository`)를 따르며,  
      새로운 패키지를 임의로 생성하지 않는다.
- 설정 변경 시
    - `application.yml` 또는 `docker-compose.yml`을 수정해야 한다면, 변경된 설정값이 실제 인프라와 맞는지 다시 확인한다.
- 테스트 코드
    - REST API 및 Kafka Consumer/Producer는 최소 단위 테스트와 통합 테스트를 작성하도록 권장한다.