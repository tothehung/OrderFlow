package com.orderflow.repository;

import com.orderflow.model.Order;
import com.orderflow.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Page<Order> findByCustomerId(String customerId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(String customerId, OrderStatus status, Pageable pageable);

    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query(value = "{ 'status': ?0 }", count = true)
    long countByStatus(OrderStatus status);

    @Query("{ 'customerId': ?0, 'createdAt': { $gte: ?1 } }")
    List<Order> findRecentOrdersByCustomer(String customerId, LocalDateTime since);
}