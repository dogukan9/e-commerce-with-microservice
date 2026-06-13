package com.shopwise.product.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwise.product.application.port.ProductUseCase;
import com.shopwise.product.infrastructure.kafka.event.OrderCreatedEvent;
import com.shopwise.product.infrastructure.kafka.event.OrderItemEvent;
import com.shopwise.product.infrastructure.kafka.event.StockReservationFailedEvent;
import com.shopwise.product.infrastructure.kafka.event.StockReservedEvent;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final ProductUseCase productUseCase;
    private final ProductEventPublisher productEventPublisher;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-created", groupId = "product-service")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String eventId = record.topic() + ":" + record.key();
        String payload = record.value();

        log.info("OrderCreated event alındı — eventId: {}", eventId);

        if (processedEventRepository.existsByEventId(eventId)) {
            log.warn("Bu event daha önce işlendi, atlanıyor — eventId: {}", eventId);
            ack.acknowledge();
            return;
        }

        try {
            String actualPayload = objectMapper.readValue(payload, String.class);
            OrderCreatedEvent event = objectMapper.readValue(actualPayload, OrderCreatedEvent.class);

            List<StockReservedEvent> reservedEvents = new ArrayList<>();
            for (OrderItemEvent item : event.items()) {
                productUseCase.reserveStock(item.productId(), item.quantity());
                reservedEvents.add(new StockReservedEvent(
                        event.orderId(),
                        item.productId(),
                        item.quantity(),
                        item.unitPrice()
                ));
            }

            processedEventRepository.save(ProcessedEventEntity.builder()
                    .eventId(eventId)
                    .eventType("OrderCreated")
                    .processedAt(LocalDateTime.now())
                    .build());


            for (StockReservedEvent reservedEvent : reservedEvents) {
                productEventPublisher.publishStockReserved(reservedEvent);
            }

            ack.acknowledge();
            log.info("OrderCreated event başarıyla işlendi — eventId: {}", eventId);

        } catch (Exception e) {
            log.error("OrderCreated event işlenemedi — eventId: {}, hata: {}", eventId, e.getMessage());

            try {
                String actualPayload = objectMapper.readValue(payload, String.class);
                OrderCreatedEvent event = objectMapper.readValue(actualPayload, OrderCreatedEvent.class);

                productEventPublisher.publishStockReservationFailed(
                        new StockReservationFailedEvent(event.orderId(), null, e.getMessage())
                );

                processedEventRepository.save(ProcessedEventEntity.builder()
                        .eventId(eventId)
                        .eventType("OrderCreated-Failed")
                        .processedAt(LocalDateTime.now())
                        .build());

                ack.acknowledge();

            } catch (Exception ex) {
                // Teknik hata -> retry -> DLT
                log.error("StockReservationFailed gönderilemedi — teknik hata: {}", ex.getMessage());
                throw new RuntimeException(ex);
            }
        }
    }
}
