package com.weverse.service;

import com.weverse.dto.*;
import com.weverse.entity.*;
import com.weverse.exception.*;
import com.weverse.producer.ClaimProducer;
import com.weverse.producer.OrderEventProducer;
import com.weverse.repository.CouponRepository;
import com.weverse.repository.MemberRepository;
import com.weverse.repository.OrderRepository;
import com.weverse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final OrderEventProducer orderEventProducer;
    private final ClaimProducer claimProducer;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException(request.getMemberId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        stockService.checkAvailability(request.getProductId(), request.getQuantity());

        long originalPrice = product.getPrice() * request.getQuantity();
        long discountedPrice = originalPrice;

        // 쿠폰 적용: 만료일 → 잔여 수량 → 사용 여부 순으로 검증한다.
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            discountedPrice = applyCouponDiscount(request.getCouponCode(), discountedPrice);
        }

        boolean isActivePremium = member.getMembershipGrade() == MembershipGrade.PREMIUM
                && member.getSubscribeEndAt() != null
                && member.getSubscribeEndAt().isAfter(LocalDateTime.now());

        if (!isActivePremium && member.getMembershipGrade() == MembershipGrade.PREMIUM) {
            log.warn("PREMIUM 구독 만료 - memberId: {}, 만료일: {}",
                    member.getMemberId(), member.getSubscribeEndAt());
        }

        if (product.getRequiredGrade() == MembershipGrade.PREMIUM) {

            // PREMIUM 멤버십 없으면 차단
            if (!isActivePremium) {
                throw new InsufficientMembershipException(
                        "PREMIUM 멤버십이 필요한 상품입니다. productId: " + product.getProductId());
            }

            // 아티스트 멤버십 불일치 시 차단
            if (!product.getArtistId().equals(member.getArtistId())) {
                throw new InsufficientMembershipException(
                        "해당 아티스트의 멤버십이 필요합니다. artistId: " + product.getArtistId());
            }
        }

        // 상품별 PREMIUM 할인율 적용
        if (isActivePremium && product.getPremiumDiscountRate() > 0) {
            discountedPrice = discountedPrice * (100 - product.getPremiumDiscountRate()) / 100;
        }

        Order order = orderRepository.save(Order.builder()
                .memberId(request.getMemberId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .originalPrice(originalPrice)
                .discountedPrice(discountedPrice)
                .couponCode(request.getCouponCode())
                .status(OrderStatus.PENDING)
                .build());

        orderEventProducer.publish(OrderEvent.builder()
                .orderId(order.getOrderId())
                .memberId(order.getMemberId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .couponCode(order.getCouponCode())
                .originalPrice(order.getOriginalPrice())
                .discountedPrice(order.getDiscountedPrice())
                .build());

        log.info("주문 생성 완료 - orderId: {}, memberId: {}, discountedPrice: {}",
                order.getOrderId(), order.getMemberId(), order.getDiscountedPrice());

        return CreateOrderResponse.builder()
                .orderId(order.getOrderId())
                .status(order.getStatus())
                .originalPrice(order.getOriginalPrice())
                .discountedPrice(order.getDiscountedPrice())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderDetailResponse.from(order);
    }

    // 컨슈머(OrderEventConsumer)에서 호출된다.
    // @Lock(PESSIMISTIC_WRITE)를 사용하는 findByProductIdForUpdate가 트랜잭션을 요구한다.
    @Transactional
    public void processOrder(Map<String, Object> payload) {
        Long orderId = ((Number) payload.get("orderId")).longValue();
        String productId = (String) payload.get("productId");
        int quantity = ((Number) payload.get("quantity")).intValue();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.updateStatus(OrderStatus.PROCESSING);

        stockService.decreaseStock(productId, quantity);

        order.updateStatus(OrderStatus.COMPLETED);

        log.info("주문 처리 완료 - orderId: {}, productId: {}, 차감 수량: {}", orderId, productId, quantity);
    }

    @Transactional
    public CancelOrderResponse cancelOrder(Long orderId, CancelOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new InvalidOrderStatusException(orderId, order.getStatus());
        }

        order.updateStatus(OrderStatus.CANCELLED);

        claimProducer.publish(ClaimEvent.builder()
                .orderId(orderId)
                .reason(request.getReason())
                .build());

        log.info("주문 취소 요청 완료 - orderId: {}, 사유: {}", orderId, request.getReason());

        return CancelOrderResponse.builder()
                .orderId(orderId)
                .status(OrderStatus.CANCELLED)
                .message("주문이 취소되었습니다.")
                .build();
    }

    private long applyCouponDiscount(String couponCode, long price) {
        Coupon coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new InvalidCouponException("존재하지 않는 쿠폰입니다: " + couponCode));

        if (coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCouponException("만료된 쿠폰입니다: " + couponCode);
        }
        if (coupon.isUsed() || coupon.getRemainQuantity() <= 0) {
            throw new InvalidCouponException("소진된 쿠폰입니다: " + couponCode);
        }

        coupon.decreaseRemainQuantity();
        return price * (100 - coupon.getDiscountRate()) / 100;
    }
}
