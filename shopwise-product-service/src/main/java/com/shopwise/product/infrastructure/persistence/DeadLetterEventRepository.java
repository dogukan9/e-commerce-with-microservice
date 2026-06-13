package com.shopwise.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DeadLetterEventRepository extends JpaRepository<DeadLetterEventEntity, Long> {
    List<DeadLetterEventEntity> findByResolvedFalse();
    void deleteByResolvedTrueAndCreatedAtBefore(LocalDateTime dateTime);
}
