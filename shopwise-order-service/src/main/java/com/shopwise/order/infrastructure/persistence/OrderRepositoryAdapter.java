package com.shopwise.order.infrastructure.persistence;

import com.shopwise.order.domain.model.Order;
import com.shopwise.order.domain.port.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(OrderEntity.fromDomain(order)).toDomain();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id).map(OrderEntity::toDomain);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderJpaRepository.findByUserId(userId)
                .stream().map(OrderEntity::toDomain).toList();
    }
}
