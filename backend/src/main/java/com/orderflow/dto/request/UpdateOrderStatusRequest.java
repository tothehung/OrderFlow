package com.orderflow.dto.request;

import com.orderflow.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String reason;
}