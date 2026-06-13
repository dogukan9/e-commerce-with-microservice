package com.shopwise.order.infrastructure.kafka;

import com.shopwise.order.infrastructure.persistence.DeadLetterEventRepository;
import com.shopwise.order.infrastructure.persistence.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxCleanupService {

    private final OutboxEventRepository outboxEventRepository;
    private final DeadLetterEventRepository deadLetterEventRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOutboxEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        outboxEventRepository.deleteByProcessedTrueAndCreatedAtBefore(threshold);
        log.info("7 günden eski processed outbox_events temizlendi");
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupDeadLetterEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        deadLetterEventRepository.deleteByResolvedTrueAndCreatedAtBefore(threshold);
        log.info("30 günden eski resolved dead_letter_events temizlendi");
    }
}
