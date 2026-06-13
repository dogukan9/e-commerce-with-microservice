package com.shopwise.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwise.order.application.dto.*;
import com.shopwise.order.application.port.OrderUseCase;
import com.shopwise.order.domain.model.Order;
import com.shopwise.order.domain.model.OrderItem;
import com.shopwise.order.domain.port.OrderRepositoryPort;
import com.shopwise.order.infrastructure.client.ProductClient;
import com.shopwise.order.infrastructure.client.UserClient;
import com.shopwise.order.infrastructure.client.dto.ProductClientResponse;
import com.shopwise.order.infrastructure.client.dto.UserClientResponse;
import com.shopwise.order.infrastructure.exception.BusinessException;
import com.shopwise.order.infrastructure.exception.ErrorCode;
import com.shopwise.order.infrastructure.kafka.event.OrderCancelledEvent;
import com.shopwise.order.infrastructure.kafka.event.OrderConfirmedEvent;
import com.shopwise.order.infrastructure.kafka.event.OrderCreatedEvent;
import com.shopwise.order.infrastructure.kafka.event.OrderItemEvent;
import com.shopwise.order.infrastructure.persistence.OutboxEventEntity;
import com.shopwise.order.infrastructure.persistence.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        String authorization = getCurrentToken();

        UserClientResponse user = userClient.getUserById(userId, authorization);
        if (!user.active()) {
            throw new BusinessException(ErrorCode.USER_NOT_ACTIVE, "Kullanıcı aktif değil: " + userId);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.items()) {
            ProductClientResponse product = productClient.getProductById(
                    itemRequest.productId(),
                    getCurrentToken()
            );

            if (!product.active()) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_ACTIVE,
                        "Ürün aktif değil: " + itemRequest.productId());
            }

            orderItems.add(OrderItem.create(
                    product.id(),
                    product.name(),
                    itemRequest.quantity(),
                    product.price()
            ));
        }

        Order order = Order.create(userId, orderItems, request.shippingAddress());
        order = setUpdatedBy(order);
        Order savedOrder = orderRepositoryPort.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderItemEvent(
                                item.getProductId(), item.getProductName(),
                                item.getQuantity(), item.getUnitPrice()))
                        .toList(),
                savedOrder.getTotalAmount(),
                savedOrder.getShippingAddress()
        );

        saveOutboxEvent("OrderCreated", savedOrder.getId(), event);
        return buildOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        return buildOrderResponse(findOrderById(id));
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepositoryPort.findByUserId(userId)
                .stream()
                .map(this::buildOrderResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = findOrderById(id);
        order.cancel();
        order = setUpdatedBy(order);
        Order savedOrder = orderRepositoryPort.save(order);

        OrderCancelledEvent event = new OrderCancelledEvent(
                savedOrder.getId(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderItemEvent(
                                item.getProductId(), item.getProductName(),
                                item.getQuantity(), item.getUnitPrice()))
                        .toList()
        );

        saveOutboxEvent("OrderCancelled", savedOrder.getId(), event);
        return buildOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse confirmOrder(Long id) {
        Order order = findOrderById(id);
        order.confirm();
        order = setUpdatedBy(order);
        Order savedOrder = orderRepositoryPort.save(order);

        OrderConfirmedEvent event = new OrderConfirmedEvent(
                savedOrder.getId(),
                savedOrder.getItems().stream()
                        .map(item -> new OrderItemEvent(
                                item.getProductId(), item.getProductName(),
                                item.getQuantity(), item.getUnitPrice()))
                        .toList()
        );

        saveOutboxEvent("OrderConfirmed", savedOrder.getId(), event);
        return buildOrderResponse(savedOrder);
    }

    private Order findOrderById(Long id) {
        return orderRepositoryPort.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                        "Sipariş bulunamadı: " + id));
    }

    private void saveOutboxEvent(String eventType, Long aggregateId, Object payload) {
        try {
            outboxEventRepository.save(OutboxEventEntity.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(eventType)
                    .aggregateId(String.valueOf(aggregateId))
                    .payload(objectMapper.writeValueAsString(payload))
                    .processed(false)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Event oluşturulamadı");
        }
    }

    private OrderResponse buildOrderResponse(Order order) {
        return OrderResponse.from(
                order,
                getAuditUser(order.getCreatedBy()),
                getAuditUser(order.getUpdatedBy())
        );
    }

    private AuditUserResponse getAuditUser(Long userId) {
        if (userId == null) return null;
        try {
            String authorization = getCurrentToken();
            UserClientResponse user = userClient.getUserById(userId, authorization);
            return new AuditUserResponse(user.id(), user.fullName());
        } catch (Exception e) {
            log.warn("Kullanıcı bilgisi alınamadı — userId: {}", userId);
            return new AuditUserResponse(userId, null);
        }
    }

    private String getCurrentToken() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return null;
        return attributes.getRequest().getHeader("Authorization");
    }

    private Order setUpdatedBy(Order order) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getName())) {
                return order.withUpdatedBy(Long.valueOf(auth.getName()));
            }
        } catch (Exception e) {
            log.warn("updatedBy set edilemedi: {}", e.getMessage());
        }
        return order;
    }
}