package com.orderflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderflow.dto.request.CreateOrderRequest;
import com.orderflow.dto.request.UpdateOrderStatusRequest;
import com.orderflow.dto.response.OrderResponse;
import com.orderflow.dto.response.PageResponse;
import com.orderflow.event.OrderEvents;
import com.orderflow.kafka.OrderEventProducer;
import com.orderflow.model.*;
import com.orderflow.repository.OrderRepository;
import com.orderflow.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventProducer eventProducer;
    private final ObjectMapper objectMapper;

    // ─── Create ──────────────────────────────────────────────────────────────

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        // Build domain object
        List<Order.OrderItem> items = req.getItems().stream()
                .map(i -> Order.OrderItem.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toList();

        Order order = Order.builder()
                .customerId(req.getCustomerId())
                .customerName(req.getCustomerName())
                .customerEmail(req.getCustomerEmail())
                .items(items)
                .status(OrderStatus.PENDING)
                .shippingAddress(Order.ShippingAddress.builder()
                        .street(req.getShippingAddress().getStreet())
                        .city(req.getShippingAddress().getCity())
                        .province(req.getShippingAddress().getProvince())
                        .postalCode(req.getShippingAddress().getPostalCode())
                        .country(req.getShippingAddress().getCountry())
                        .build())
                .notes(req.getNotes())
                .build();

        order.setTotalAmount(order.calculateTotal());

        Order saved = orderRepository.save(order);

        // Outbox pattern: save event atomically with order data
        saveOutboxEvent(saved.getId(), "Order", "ORDER_CREATED",
                buildOrderCreatedEvent(saved));

        log.info("Created order: id={} customer={}", saved.getId(), saved.getCustomerId());

        // Publish to Kafka directly (outbox poller is the fallback)
        eventProducer.publishOrderCreated(buildOrderCreatedEvent(saved));

        return OrderResponse.from(saved);
    }

    // ─── Read ────────────────────────────────────────────────────────────────

    @Cacheable(value = "orders", key = "#id")
    public OrderResponse getOrderById(String id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));
    }

    public PageResponse<OrderResponse> getAllOrders(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<OrderResponse> result = orderRepository.findAll(pageable).map(OrderResponse::from);
        return PageResponse.from(result);
    }

    public PageResponse<OrderResponse> getOrdersByCustomer(String customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderResponse> result = orderRepository.findByCustomerId(customerId, pageable).map(OrderResponse::from);
        return PageResponse.from(result);
    }

    public PageResponse<OrderResponse> getOrdersByStatus(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<OrderResponse> result = orderRepository.findByStatus(status, pageable).map(OrderResponse::from);
        return PageResponse.from(result);
    }

    // ─── Update ──────────────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public OrderResponse updateOrderStatus(String id, UpdateOrderStatusRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        OrderStatus previousStatus = order.getStatus();

        if (!order.canTransitionTo(req.getStatus())) {
            throw new InvalidOrderTransitionException(
                    String.format("Cannot transition from %s to %s", previousStatus, req.getStatus()));
        }

        order.setStatus(req.getStatus());
        Order updated = orderRepository.save(order);

        // Build and publish event
        OrderEvents.OrderStatusUpdated event = OrderEvents.OrderStatusUpdated.builder()
                .orderId(id)
                .customerId(order.getCustomerId())
                .previousStatus(previousStatus)
                .newStatus(req.getStatus())
                .reason(req.getReason())
                .updatedAt(LocalDateTime.now())
                .build();

        saveOutboxEvent(id, "Order", "ORDER_STATUS_UPDATED", event);

        if (req.getStatus() == OrderStatus.CANCELLED) {
            OrderEvents.OrderCancelled cancelEvent = OrderEvents.OrderCancelled.builder()
                    .orderId(id)
                    .customerId(order.getCustomerId())
                    .reason(req.getReason())
                    .refundAmount(order.getTotalAmount())
                    .cancelledAt(LocalDateTime.now())
                    .build();
            eventProducer.publishOrderCancelled(cancelEvent);
        } else {
            eventProducer.publishOrderStatusUpdated(event);
        }

        log.info("Updated order status: id={} {} -> {}", id, previousStatus, req.getStatus());
        return OrderResponse.from(updated);
    }

    // ─── Delete / Cancel ─────────────────────────────────────────────────────

    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public void cancelOrder(String id, String reason) {
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest();
        req.setStatus(OrderStatus.CANCELLED);
        req.setReason(reason);
        updateOrderStatus(id, req);
    }

    // ─── Stats ───────────────────────────────────────────────────────────────

    @Cacheable(value = "order-stats", key = "'summary'")
    public OrderStats getStats() {
        return OrderStats.builder()
                .pending(orderRepository.countByStatus(OrderStatus.PENDING))
                .confirmed(orderRepository.countByStatus(OrderStatus.CONFIRMED))
                .processing(orderRepository.countByStatus(OrderStatus.PROCESSING))
                .shipped(orderRepository.countByStatus(OrderStatus.SHIPPED))
                .delivered(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .cancelled(orderRepository.countByStatus(OrderStatus.CANCELLED))
                .total(orderRepository.count())
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private OrderEvents.OrderCreated buildOrderCreatedEvent(Order order) {
        List<OrderEvents.OrderItemData> items = order.getItems().stream()
                .map(i -> OrderEvents.OrderItemData.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toList();

        return OrderEvents.OrderCreated.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .items(items)
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private void saveOutboxEvent(String aggregateId, String aggregateType, String eventType, Object payload) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateId(aggregateId)
                    .aggregateType(aggregateType)
                    .eventType(eventType)
                    .payload(objectMapper.writeValueAsString(payload))
                    .published(false)
                    .retryCount(0)
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox event for aggregateId={}", aggregateId, e);
        }
    }

    // ─── Nested types ────────────────────────────────────────────────────────

    @lombok.Data
    @lombok.Builder
    public static class OrderStats {
        private long pending, confirmed, processing, shipped, delivered, cancelled, total;
    }

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) { super(message); }
    }

    public static class InvalidOrderTransitionException extends RuntimeException {
        public InvalidOrderTransitionException(String message) { super(message); }
    }
}