package com.shopwise.order.api;

import com.shopwise.order.application.dto.CreateOrderRequest;
import com.shopwise.order.application.dto.OrderResponse;
import com.shopwise.order.application.port.OrderUseCase;
import com.shopwise.order.infrastructure.kafka.DeadLetterRetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;
    private final DeadLetterRetryService deadLetterRetryService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderUseCase.createOrder(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderUseCase.getOrderById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderUseCase.getOrdersByUserId(userId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderUseCase.cancelOrder(id));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderUseCase.confirmOrder(id));
    }

    @PostMapping("/dead-letters/{id}/retry")
    public ResponseEntity<Void> retryDeadLetter(@PathVariable Long id) {
        deadLetterRetryService.retryEvent(id);
        return ResponseEntity.ok().build();
    }
}
