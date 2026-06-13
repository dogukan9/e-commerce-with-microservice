package com.shopwise.product.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwise.product.application.port.ProductUseCase;
import com.shopwise.product.infrastructure.kafka.event.OrderCancelledEvent;
import com.shopwise.product.infrastructure.persistence.ProcessedEventEntity;
import com.shopwise.product.infrastructure.persistence.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledEventConsumer {

    private final ProductUseCase productUseCase;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-cancelled", groupId = "product-service")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String eventId = record.topic() + ":" + record.key();
        String payload = record.value();

        log.info("OrderCancelled event alındı — eventId: {}", eventId);

        if (processedEventRepository.existsByEventId(eventId)) {
            log.warn("Bu event daha önce işlendi, atlanıyor — eventId: {}", eventId);
            ack.acknowledge();
            return;
        }

        try {
             String actualPayload = objectMapper.readValue(payload, String.class);
            OrderCancelledEvent event = objectMapper.readValue(actualPayload, OrderCancelledEvent.class);
            for (var item : event.items()) {
                productUseCase.releaseReservation(item.productId(), item.quantity());
                log.info("Stok rezervi kaldırıldı — productId: {}", item.productId());
            }

            processedEventRepository.save(ProcessedEventEntity.builder()
                    .eventId(eventId)
                    .eventType("OrderCancelled")
                    .processedAt(LocalDateTime.now())
                    .build());

            ack.acknowledge();
            log.info("OrderCancelled event başarıyla işlendi — eventId: {}", eventId);

        } catch (Exception e) {
            log.error("OrderCancelled event işlenemedi — eventId: {}, hata: {}", eventId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
