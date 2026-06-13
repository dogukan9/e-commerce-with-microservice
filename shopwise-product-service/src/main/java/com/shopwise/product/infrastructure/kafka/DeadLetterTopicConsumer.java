package com.shopwise.product.infrastructure.kafka;

import com.shopwise.product.infrastructure.persistence.DeadLetterEventEntity;
import com.shopwise.product.infrastructure.persistence.DeadLetterEventRepository;
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

    @KafkaListener(topics = "order-created-dlt", groupId = "product-service-dlt")
    public void consumeOrderCreatedDlt(ConsumerRecord<String, String> record) {
        log.error("OrderCreated DLT alındı — key: {}", record.key());
        saveDlt(record, "3 kez denendi, teknik hata nedeniyle işlenemedi");
    }

    @KafkaListener(topics = "order-cancelled-dlt", groupId = "product-service-dlt")
    public void consumeOrderCancelledDlt(ConsumerRecord<String, String> record) {
        log.error("OrderCancelled DLT alındı — key: {}", record.key());
        saveDlt(record, "3 kez denendi, stok rezervi kaldırılamadı");
    }

    @KafkaListener(topics = "order-confirmed-dlt", groupId = "product-service-dlt")
    public void consumeOrderConfirmedDlt(ConsumerRecord<String, String> record) {
        log.error("OrderConfirmed DLT alındı — key: {}", record.key());
        saveDlt(record, "3 kez denendi, stok rezervi onaylanamadı");
    }

    private void saveDlt(ConsumerRecord<String, String> record, String reason) {
        deadLetterEventRepository.save(DeadLetterEventEntity.builder()
                .topic(record.topic())
                .eventKey(record.key() != null ? record.key() : "unknown")
                .payload(record.value())
                .reason(reason)
                .resolved(false)
                .createdAt(LocalDateTime.now())
                .build());

        log.error("Dead letter DB'ye kaydedildi — DeadLetterRetryService halleder");
    }
}