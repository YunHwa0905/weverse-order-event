package com.weverse.repository;

import com.weverse.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, String> {

    // 재고 차감 시 동시 주문에 의한 이중 차감을 막기 위해 비관적 쓰기 락을 사용한다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.productId = :productId")
    Optional<Stock> findByProductIdForUpdate(@Param("productId") String productId);
}
