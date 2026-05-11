package com.orderflow.controller;

import com.orderflow.dto.request.CreateOrderRequest;
import com.orderflow.dto.request.UpdateOrderStatusRequest;
import com.orderflow.dto.response.ApiResponse;
import com.orderflow.dto.response.PageResponse;
import com.orderflow.dto.response.OrderResponse;
import com.orderflow.model.OrderStatus;
import com.orderflow.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order created successfully", order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrderById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerId
    ) {
        PageResponse<OrderResponse> result;

        if (customerId != null) {
            result = orderService.getOrdersByCustomer(customerId, page, size);
        } else if (status != null) {
            result = orderService.getOrdersByStatus(status, page, size);
        } else {
            result = orderService.getAllOrders(page, size, sortBy);
        }

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        OrderResponse updated = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Order status updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Customer requested cancellation") String reason
    ) {
        orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<OrderService.OrderStats>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getStats()));
    }
}