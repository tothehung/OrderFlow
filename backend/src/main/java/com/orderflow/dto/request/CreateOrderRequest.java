package com.orderflow.dto.request;

import com.orderflow.model.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100)
    private String customerName;

    @Email(message = "Valid email is required")
    @NotBlank
    private String customerEmail;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Valid
    @NotNull(message = "Shipping address is required")
    private ShippingAddressRequest shippingAddress;

    @Size(max = 500)
    private String notes;

    @Data
    public static class OrderItemRequest {
        @NotBlank(message = "Product ID is required")
        private String productId;

        @NotBlank
        private String productName;

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 999)
        private Integer quantity;

        @NotNull
        @DecimalMin(value = "0.01", message = "Unit price must be positive")
        private BigDecimal unitPrice;
    }

    @Data
    public static class ShippingAddressRequest {
        @NotBlank private String street;
        @NotBlank private String city;
        @NotBlank private String province;
        @NotBlank private String postalCode;
        @NotBlank private String country;
    }
}