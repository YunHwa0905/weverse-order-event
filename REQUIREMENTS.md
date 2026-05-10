# weverse-order-event - requirements.md

## 1. 프로젝트 개요

### 프로젝트명
weverse-order-event

### 프로젝트 목적
Kafka 기반 비동기 주문 이벤트 처리 시스템을 구현한다.

주문 생성 이벤트를 Kafka Topic으로 전달하고,
Consumer에서 주문 처리, 재고 차감, 알림 발행을 수행한다.

Redis 캐시를 활용하여 재고 조회 성능을 개선하고,
Dead Letter Queue(DLQ)를 통해 실패 이벤트를 관리한다.

---

# 2. 핵심 기능 요구사항

## 2.1 주문 생성

### API
POST /api/orders

### 요청 예시
```json
{
  "memberId": "member-001",
  "productId": "product-001",
  "quantity": 2
}
```

### 처리 요구사항
- 주문 상태는 최초 `PENDING`
- Kafka `order-topic`에 주문 이벤트 발행
- 이벤트는 JSON 형식 사용
- Producer는 파티션 0 또는 1로 랜덤 분배

### 응답 예시
```json
{
  "orderId": 1,
  "status": "PENDING"
}
```

---

## 2.2 주문 이벤트 처리

### Kafka Consumer
- `order-topic` 구독
- Consumer Group: `weverse-order-group`

### 처리 흐름
1. 주문 이벤트 수신
2. 주문 상태 `PROCESSING`
3. 재고 확인
4. 재고 차감
5. MySQL 저장
6. 주문 상태 `COMPLETED`
7. 알림 이벤트 발행

### 실패 처리
- 예외 발생 시:
    - 상태 `FAILED`
    - `order-dead-letter-topic` 전송
    - ERROR 로그 기록

---

## 2.3 주문 조회

### API
GET /api/orders/{orderId}

### 응답 요구사항
- 주문 정보 반환
- 상태값 포함
- 생성/수정 시간 포함

---

## 2.4 재고 조회

### API
GET /api/orders/stock/{productId}

### 처리 요구사항
1. Redis 우선 조회
2. Cache Miss 시 MySQL 조회
3. 조회 결과 Redis 저장

### 응답 예시
```json
{
  "productId": "product-001",
  "stock": 98,
  "cached": true
}
```

---

# 3. Kafka 요구사항

## 사용 토픽

### order-topic
- 주문 이벤트 처리
- 파티션 수: 2

### order-dead-letter-topic
- 실패 이벤트 저장

### order-notification-topic
- 주문 완료 알림 이벤트

---

# 4. Redis 캐시 요구사항

## 캐시 정책
- Cache Aside 패턴 사용

## 재고 조회
- Redis 우선 조회
- 미스 시 DB 조회 후 캐싱

## 재고 감소
- 주문 성공 시 Redis 재고 감소

---

# 5. 데이터베이스 요구사항

## Order Entity

### 필드
- orderId
- memberId
- productId
- quantity
- status
- createdAt
- updatedAt

### 상태값
- PENDING
- PROCESSING
- COMPLETED
- FAILED

---

# 6. 예외 처리 요구사항

- 전역 예외 처리 적용
- 비즈니스 예외 클래스 분리
- Kafka 처리 실패 시 DLQ 전송

---

# 7. 로깅 요구사항

## INFO
- 주문 생성
- Kafka 발행 성공
- 주문 완료

## WARN
- Redis Cache Miss
- 재시도 처리

## ERROR
- Consumer 처리 실패
- DB 저장 실패
- Kafka 예외

---

# 8. 테스트 요구사항

## 단위 테스트
- Service
- Producer
- Consumer

## 통합 테스트
- Kafka 이벤트 처리
- Redis 연동
- API 테스트

---

# 9. 인프라 요구사항

## Docker Compose 구성
- Kafka (KRaft)
- Redis
- MySQL
- Kafdrop

---

# 10. 구현 우선순위

1. Docker Compose 구성
2. Spring Boot 프로젝트 생성
3. Kafka 설정
4. Producer 구현
5. Consumer 구현
6. Entity/Repository 구현
7. Redis 캐시 구현
8. DLQ 처리
9. REST API 구현
10. 테스트 코드 작성
11. README 작성