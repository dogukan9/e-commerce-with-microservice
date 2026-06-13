package com.shopwise.order.infrastructure.kafka;

import com.shopwise.order.infrastructure.persistence.DeadLetterEventEntity;
import com.shopwise.order.infrastructure.persistence.DeadLetterEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterTopicConsumer {
    private final DeadLetterEventRepository deadLetterEventRepository;

    // StockReservationFailed 3 retry sonrası başarısız -> DLT
    @KafkaListener(topics = "stock-reservation-failed-dlt", groupId = "order-service-dlt")
    public void consumeStockReservationFailedDlt(ConsumerRecord<String, String> record) {
        log.error(" StockReservationFailed DLT alındı — key: {}", record.key());

        deadLetterEventRepository.save(DeadLetterEventEntity.builder()
                .topic(record.topic())
                .eventKey(record.key() != null ? record.key() : "unknown")
                .payload(record.value())
                .reason("3 kez denendi, işlenemedi")
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build());

        log.error("Dead letter DB'ye kaydedildi — manuel müdahale gerekebilir");
    }

    // StockReserved 3 retry sonrası başarısız -> DLT
    @KafkaListener(topics = "stock-reserved-dlt", groupId = "order-service-dlt")
    public void consumeStockReservedDlt(ConsumerRecord<String, String> record) {
        log.error("StockReserved DLT alındı — key: {}", record.key());

        deadLetterEventRepository.save(DeadLetterEventEntity.builder()
                .topic(record.topic())
                .eventKey(record.key() != null ? record.key() : "unknown")
                .payload(record.value())
                .reason("StockReserved 3 kez denendi, işlenemedi — sipariş iptal ediliyor")
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build());
        log.error("Dead letter DB'ye kaydedildi — DeadLetterRetryService halleder");

    }
}
