package com.shopwise.order.infrastructure.kafka;

import com.shopwise.order.infrastructure.persistence.OutboxEventEntity;
import com.shopwise.order.infrastructure.persistence.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWorker {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEventEntity> events = outboxEventRepository.findByProcessedFalse();

        for (OutboxEventEntity event : events) {
            try {
                kafkaTemplate.send(
                        resolveTopicName(event.getEventType()),
                        event.getEventId(),   // key olarak eventId (UUID) kullanıyoruz
                        event.getPayload()
                );

                event.setProcessed(true);
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

                log.info("Outbox event Kafka'ya gönderildi: {} — {}", event.getEventType(), event.getEventId());

            } catch (Exception e) {
                log.error("Outbox event gönderilemedi: {} — hata: {}", event.getEventId(), e.getMessage());
            }
        }
    }

    private String resolveTopicName(String eventType) {
        return switch (eventType) {
            case "OrderCreated" -> "order-created";
            case "OrderCancelled" -> "order-cancelled";
            case "OrderConfirmed" -> "order-confirmed";
            default -> throw new IllegalArgumentException("Bilinmeyen event tipi: " + eventType);
        };
    }
}
