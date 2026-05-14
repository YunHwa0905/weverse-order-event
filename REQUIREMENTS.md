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

멤버십 구독, 쿠폰 적용, 클레임(취소/환불) 기능을 추가하여
위버스 커머스 핵심 도메인을 재현한다.

---

## 2. 핵심 기능 요구사항

## 2.1 주문 생성

### API
POST /api/orders

### 요청 예시
```json
{
  "memberId": "member-001",
  "productId": "product-001",
  "quantity": 2,
  "couponCode": "WELCOME10"
}
```

### 처리 요구사항
- 주문 상태는 최초 PENDING
- 쿠폰 코드 입력 시 유효성 검증 후 할인 적용
- PREMIUM 멤버십 회원은 추가 할인 적용
- Kafka order-topic에 주문 이벤트 발행
- 이벤트는 JSON 형식 사용
- Producer는 파티션 0 또는 1로 랜덤 분배

### 응답 예시
```json
{
  "orderId": 1,
  "status": "PENDING",
  "originalPrice": 30000,
  "discountedPrice": 27000
}
```

---

## 2.2 주문 이벤트 처리

### Kafka Consumer
- order-topic 구독
- Consumer Group: weverse-order-group

### 처리 흐름
1. 주문 이벤트 수신
2. 주문 상태 PROCESSING
3. 재고 확인
4. 재고 차감
5. MySQL 저장
6. 주문 상태 COMPLETED
7. 알림 이벤트 발행 (order-notification-topic)

### 실패 처리
- 예외 발생 시:
  - 상태 FAILED
  - order-dead-letter-topic 전송
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

## 2.5 멤버십 구독

### API
POST /api/members/subscribe

### 요청 예시
```json
{
  "memberId": "member-001",
  "grade": "PREMIUM"
}
```

### 처리 요구사항
- 멤버십 등급: FREE / PREMIUM
- 구독 시작일/만료일 저장
- Redis에 멤버십 상태 캐싱
- Kafka membership-topic에 이벤트 발행

### 응답 예시
```json
{
  "memberId": "member-001",
  "grade": "PREMIUM",
  "subscribeEndAt": "2027-05-08T00:00:00"
}
```

---

## 2.6 멤버십 상태 조회

### API
GET /api/members/{memberId}/status

### 응답 요구사항
- 멤버십 등급 포함
- 구독 만료일 포함
- Redis 우선 조회

---

## 2.7 쿠폰 발급

### API
POST /api/coupons/issue

### 요청 예시
```json
{
  "memberId": "member-001",
  "couponCode": "WELCOME10"
}
```

### 처리 요구사항
- 쿠폰 유효성 검증 (만료일, 잔여 수량)
- Redis로 쿠폰 잔여 수량 관리 (동시성 방지)
- 사용된 쿠폰 중복 사용 방지

### 응답 예시
```json
{
  "couponCode": "WELCOME10",
  "discountRate": 10,
  "expiredAt": "2026-12-31T23:59:59"
}
```

---

## 2.8 주문 취소/환불 (클레임)

### API
POST /api/orders/{orderId}/claim

### 요청 예시
```json
{
  "reason": "단순 변심"
}
```

### 처리 요구사항
- 주문 상태가 COMPLETED일 때만 취소 가능
- Kafka claim-topic에 클레임 이벤트 발행
- Consumer에서 재고 복구 처리
- 주문 상태 CANCELLED로 변경
- 환불 알림 이벤트 발행 (order-notification-topic)

### 응답 예시
```json
{
  "orderId": 1,
  "status": "CANCELLED",
  "message": "주문이 취소되었습니다."
}
```

---

## 3. Kafka 요구사항

### 사용 토픽

#### order-topic
- 주문 이벤트 처리
- 파티션 수: 2

#### order-dead-letter-topic
- 실패 이벤트 저장
- 파티션 수: 1

#### order-notification-topic
- 주문 완료/취소 알림 이벤트
- 파티션 수: 1

#### claim-topic
- 주문 취소/환불 클레임 이벤트
- 파티션 수: 2

#### membership-topic
- 멤버십 구독 이벤트
- 파티션 수: 1

---

## 4. Redis 캐시 요구사항

### 캐시 정책
- Cache Aside 패턴 사용

### 재고 조회
- Redis 우선 조회
- 미스 시 DB 조회 후 캐싱

### 재고 감소
- 주문 성공 시 Redis 재고 감소
- 클레임(취소) 성공 시 Redis 재고 복구

### 멤버십 상태
- 멤버십 구독 시 Redis 캐싱
- 주문 시 Redis에서 멤버십 등급 조회

### 쿠폰 잔여 수량
- Redis로 원자적 감소 처리 (동시성 방지)

---

## 5. 데이터베이스 요구사항

### Order Entity

#### 필드
- orderId
- memberId
- productId
- quantity
- originalPrice
- discountedPrice
- couponCode (nullable)
- status
- createdAt
- updatedAt

#### 상태값
- PENDING
- PROCESSING
- COMPLETED
- FAILED
- CANCELLED

---

### Member Entity

#### 필드
- memberId (PK, String)
- membershipGrade (FREE / PREMIUM)
- subscribeStartAt
- subscribeEndAt
- createdAt

---

### Coupon Entity

#### 필드
- couponId
- couponCode (unique)
- discountRate
- totalQuantity
- remainQuantity
- expiredAt
- used

---

### Stock Entity

#### 필드
- productId (PK, String)
- quantity

---

## 6. 예외 처리 요구사항

- 전역 예외 처리 적용
- 비즈니스 예외 클래스 분리
- Kafka 처리 실패 시 DLQ 전송
- 재고 부족 예외 처리
- 쿠폰 유효하지 않음 예외 처리
- 취소 불가 상태 예외 처리

---

## 7. 로깅 요구사항

### INFO
- 주문 생성
- Kafka 발행 성공
- 주문 완료
- 멤버십 구독 완료
- 쿠폰 발급 완료
- 클레임 처리 완료

### WARN
- Redis Cache Miss
- 재시도 처리

### ERROR
- Consumer 처리 실패
- DB 저장 실패
- Kafka 예외
- 재고 부족
- 쿠폰 유효성 실패

---

## 8. 테스트 요구사항

### 단위 테스트
- Service (OrderService, StockService, MemberService, CouponService)
- Producer (OrderEventProducer, ClaimProducer)
- Consumer (OrderEventConsumer, ClaimConsumer)

### 통합 테스트
- Kafka 이벤트 처리
- Redis 연동
- API 테스트

---

## 9. 인프라 요구사항

### Docker Compose 구성
- Kafka (KRaft 모드, Zookeeper 없음)
- Redis 7.2
- MySQL 8.0
- Kafdrop

---

## 10. 구현 우선순위

1. Docker Compose 구성 (완료)
2. Spring Boot 프로젝트 생성 (완료)
3. Kafka 설정 (Topic 5개)
4. Producer 구현 (주문 + 클레임 + 멤버십)
5. Consumer 구현 (주문 + 클레임 + 멤버십 + DLQ)
6. Entity/Repository 구현 (Order, Member, Coupon, Stock)
7. Redis 캐시 구현 (재고 + 멤버십 + 쿠폰)
8. DLQ 처리
9. REST API 구현
10. Swagger 문서화
11. 테스트 코드 작성
12. README 작성