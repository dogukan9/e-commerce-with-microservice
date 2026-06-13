package com.shopwise.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {
    List<OutboxEventEntity> findByProcessedFalse();
    void deleteByProcessedTrueAndCreatedAtBefore(LocalDateTime dateTime);
}
