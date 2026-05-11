package com.orderflow.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    @Indexed
    private String customerId;

    private String customerName;
    private String customerEmail;

    private List<OrderItem> items;

    @Indexed
    private OrderStatus status;

    private BigDecimal totalAmount;

    private ShippingAddress shippingAddress;

    private String notes;

    // Optimistic locking — prevents concurrent update conflicts
    @Version
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // ─── Nested types ────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;

        public BigDecimal getSubtotal() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddress {
        private String street;
        private String city;
        private String province;
        private String postalCode;
        private String country;
    }

    // ─── Domain logic ────────────────────────────────────────────────────────

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this.status) {
            case PENDING    -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED  -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED;
            case SHIPPED    -> newStatus == OrderStatus.DELIVERED;
            default         -> false; // DELIVERED, CANCELLED are terminal
        };
    }

    public BigDecimal calculateTotal() {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}