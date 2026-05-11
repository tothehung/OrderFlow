package com.orderflow.dto.response;

import com.orderflow.model.Order;
import com.orderflow.model.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private List<OrderItemResponse> items;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private ShippingAddressResponse shippingAddress;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class OrderItemResponse {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }

    @Data
    @Builder
    public static class ShippingAddressResponse {
        private String street;
        private String city;
        private String province;
        private String postalCode;
        private String country;
    }

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems() == null ? List.of() : order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .subtotal(item.getSubtotal())
                                .build())
                        .toList())
                .shippingAddress(order.getShippingAddress() == null ? null :
                        ShippingAddressResponse.builder()
                                .street(order.getShippingAddress().getStreet())
                                .city(order.getShippingAddress().getCity())
                                .province(order.getShippingAddress().getProvince())
                                .postalCode(order.getShippingAddress().getPostalCode())
                                .country(order.getShippingAddress().getCountry())
                                .build())
                .build();
    }
}