package com.weverse.service;

import com.weverse.exception.OrderNotFoundException;
import com.weverse.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    private final OrderRepository orderRepository;
    private final StockService stockService;

    public void processClaim(Map<String, Object> payload) {
        Long orderId = ((Number) payload.get("orderId")).longValue();

        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        stockService.increaseStock(order.getProductId(), order.getQuantity());

        log.info("클레임 처리 완료 - orderId: {}, productId: {}, 복구 수량: {}",
                orderId, order.getProductId(), order.getQuantity());
    }
}
