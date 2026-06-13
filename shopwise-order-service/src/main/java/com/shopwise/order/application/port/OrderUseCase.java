package com.shopwise.order.application.port;

import com.shopwise.order.application.dto.CreateOrderRequest;
import com.shopwise.order.application.dto.OrderResponse;

import java.util.List;

public interface OrderUseCase {
    OrderResponse createOrder(Long userId, CreateOrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getOrdersByUserId(Long userId);
    OrderResponse cancelOrder(Long id);
    OrderResponse confirmOrder(Long id);
}
