package com.weverse.repository;

import com.weverse.entity.Order;
import com.weverse.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMemberId(String memberId);

    List<Order> findByStatus(OrderStatus status);
}
