package com.shopwise.product.infrastructure.kafka;

import com.shopwise.product.infrastructure.persistence.DeadLetterEventRepository;
import com.shopwise.product.infrastructure.persistence.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessedEventCleanupService {

    private final ProcessedEventRepository processedEventRepository;
    private final DeadLetterEventRepository deadLetterEventRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupProcessedEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        processedEventRepository.deleteByProcessedAtBefore(threshold);
        log.info("7 günden eski processed_events temizlendi");
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupDeadLetterEvents() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        deadLetterEventRepository.deleteByResolvedTrueAndCreatedAtBefore(threshold);
        log.info("30 günden eski resolved dead_letter_events temizlendi");
    }
}
