package com.orderflow.event;

import com.orderflow.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderEvents {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderCreated {
        private String orderId;
        private String customerId;
        private String customerName;
        private String customerEmail;
        private List<OrderItemData> items;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderStatusUpdated {
        private String orderId;
        private String customerId;
        private OrderStatus previousStatus;
        private OrderStatus newStatus;
        private String reason;
        private LocalDateTime updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderCancelled {
        private String orderId;
        private String customerId;
        private String reason;
        private BigDecimal refundAmount;
        private LocalDateTime cancelledAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemData {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}