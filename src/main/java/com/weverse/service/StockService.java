package com.weverse.service;

import com.weverse.dto.StockResponse;
import com.weverse.entity.Stock;
import com.weverse.exception.InsufficientStockException;
import com.weverse.exception.StockNotFoundException;
import com.weverse.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private static final String STOCK_CACHE_KEY_PREFIX = "stock:";

    private final StockRepository stockRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public StockResponse getStock(String productId) {
        String cacheKey = STOCK_CACHE_KEY_PREFIX + productId;

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            // GenericJackson2JsonRedisSerializer는 숫자를 Integer로 역직렬화하므로 Number로 받아 변환한다.
            int stockValue = ((Number) cached).intValue();
            return StockResponse.builder()
                    .productId(productId)
                    .stock(stockValue)
                    .cached(true)
                    .build();
        }

        log.warn("재고 캐시 미스 - productId: {}", productId);

        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() -> new StockNotFoundException(productId));

        redisTemplate.opsForValue().set(cacheKey, stock.getQuantity());

        return StockResponse.builder()
                .productId(productId)
                .stock(stock.getQuantity())
                .cached(false)
                .build();
    }

    public void checkAvailability(String productId, int quantity) {
        Stock stock = stockRepository.findById(productId)
                .orElseThrow(() -> new StockNotFoundException(productId));
        if (!stock.isAvailable(quantity)) {
            throw new InsufficientStockException(productId);
        }
    }

    // @Lock(PESSIMISTIC_WRITE) 쿼리를 사용하므로 반드시 트랜잭션 내에서 실행되어야 한다.
    // 호출자(processOrder)가 이미 @Transactional이면 해당 트랜잭션에 합류한다.
    @Transactional
    public void decreaseStock(String productId, int quantity) {
        String cacheKey = STOCK_CACHE_KEY_PREFIX + productId;

        Stock stock = stockRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new InsufficientStockException(productId));

        if (!stock.isAvailable(quantity)) {
            throw new InsufficientStockException(productId);
        }

        stock.decrease(quantity);

        // MySQL 차감 후 Redis에도 반영한다.
        // 트랜잭션 롤백 시 Redis는 보정되지 않으나, 다음 캐시 미스 시 DB에서 재로딩된다.
        redisTemplate.opsForValue().set(cacheKey, stock.getQuantity());

        log.info("재고 차감 - productId: {}, 차감: {}, 잔여: {}", productId, quantity, stock.getQuantity());
    }

    @Transactional
    public void increaseStock(String productId, int quantity) {
        String cacheKey = STOCK_CACHE_KEY_PREFIX + productId;

        Stock stock = stockRepository.findByProductIdForUpdate(productId)
                .orElseThrow(() -> new StockNotFoundException(productId));

        stock.increase(quantity);

        redisTemplate.opsForValue().set(cacheKey, stock.getQuantity());

        log.info("재고 복구 - productId: {}, 복구: {}, 잔여: {}", productId, quantity, stock.getQuantity());
    }
}
