package com.shopwise.product.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, Long> {
    boolean existsByEventId(String eventId);
    void deleteByProcessedAtBefore(LocalDateTime dateTime);
}
