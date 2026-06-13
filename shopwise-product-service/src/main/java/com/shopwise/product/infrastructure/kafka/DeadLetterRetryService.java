package com.shopwise.product.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwise.product.application.port.ProductUseCase;
import com.shopwise.product.infrastructure.kafka.event.*;
import com.shopwise.product.infrastructure.persistence.DeadLetterEventEntity;
import com.shopwise.product.infrastructure.persistence.DeadLetterEventRepository;
import com.shopwise.product.infrastructure.persistence.ProcessedEventEntity;
import com.shopwise.product.infrastructure.persistence.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterRetryService {

    private final DeadLetterEventRepository deadLetterEventRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ProductUseCase productUseCase;
    private final ProductEventPublisher productEventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 3600000)
    public void retryDeadLetterEvents() {
        List<DeadLetterEventEntity> events = deadLetterEventRepository.findByResolvedFalse();
        if (events.isEmpty()) return;
        log.info("Dead letter retry başladı — {} event işlenecek", events.size());
        for (DeadLetterEventEntity event : events) {
            try {
                retryEvent(event.getId());
            } catch (Exception e) {
                log.error("Dead letter retry başarısız — id: {}", event.getId());
            }
        }
    }

    @Transactional
    public void retryEvent(Long deadLetterEventId) {
        DeadLetterEventEntity deadLetterEvent = deadLetterEventRepository.findById(deadLetterEventId)
                .orElseThrow(() -> new RuntimeException("Dead letter event bulunamadı: " + deadLetterEventId));

        String originalTopic = deadLetterEvent.getTopic().replace("-dlt", "");
        String processedEventId = originalTopic + ":" + deadLetterEvent.getEventKey();

        if (processedEventRepository.existsByEventId(processedEventId)) {
            deadLetterEvent.setResolved(true);
            deadLetterEvent.setResolvedAt(LocalDateTime.now());
            deadLetterEventRepository.save(deadLetterEvent);
            log.info("Event zaten işlenmiş, resolved yapıldı — id: {}", deadLetterEventId);
            return;
        }

        try {
            switch (originalTopic) {
                case "order-created" -> retryOrderCreated(deadLetterEvent, processedEventId);
                case "order-cancelled" -> retryOrderCancelled(deadLetterEvent, processedEventId);
                case "order-confirmed" -> retryOrderConfirmed(deadLetterEvent, processedEventId);
                default -> log.error("Bilinmeyen topic: {}", originalTopic);
            }

            deadLetterEvent.setResolved(true);
            deadLetterEvent.setResolvedAt(LocalDateTime.now());
            deadLetterEventRepository.save(deadLetterEvent);

            log.info("Dead letter event çözüldü — id: {}", deadLetterEventId);

        } catch (Exception e) {
            log.error("Dead letter retry başarısız — id: {}, hata: {}", deadLetterEventId, e.getMessage());

            // Sadece OrderCreated için StockReservationFailed yayınla
            if ("order-created".equals(originalTopic)) {
                try {
                    String actualPayload = objectMapper.readValue(deadLetterEvent.getPayload(), String.class);
                    OrderCreatedEvent event = objectMapper.readValue(actualPayload, OrderCreatedEvent.class);
                    productEventPublisher.publishStockReservationFailed(
                            new StockReservationFailedEvent(event.orderId(), null, e.getMessage())
                    );
                } catch (Exception ex) {
                    log.error("StockReservationFailed de gönderilemedi: {}", ex.getMessage());
                }
            }
        }
    }

    private void retryOrderCreated(DeadLetterEventEntity deadLetterEvent, String processedEventId) throws Exception {
        String actualPayload = objectMapper.readValue(deadLetterEvent.getPayload(), String.class);
        OrderCreatedEvent event = objectMapper.readValue(actualPayload, OrderCreatedEvent.class);

        List<StockReservedEvent> reservedEvents = new ArrayList<>();
        for (OrderItemEvent item : event.items()) {
            productUseCase.reserveStock(item.productId(), item.quantity());
            reservedEvents.add(new StockReservedEvent(
                    event.orderId(), item.productId(), item.quantity(), item.unitPrice()
            ));
        }

        processedEventRepository.save(ProcessedEventEntity.builder()
                .eventId(processedEventId)
                .eventType("OrderCreated-DLT-Retry")
                .processedAt(LocalDateTime.now())
                .build());

        for (StockReservedEvent reservedEvent : reservedEvents) {
            productEventPublisher.publishStockReserved(reservedEvent);
        }

        log.info("OrderCreated DLT retry başarılı — orderId: {}", event.orderId());
    }

    private void retryOrderCancelled(DeadLetterEventEntity deadLetterEvent, String processedEventId) throws Exception {
        OrderCancelledEvent event = objectMapper.readValue(deadLetterEvent.getPayload(), OrderCancelledEvent.class);

        for (var item : event.items()) {
            productUseCase.releaseReservation(item.productId(), item.quantity());
        }

        processedEventRepository.save(ProcessedEventEntity.builder()
                .eventId(processedEventId)
                .eventType("OrderCancelled-DLT-Retry")
                .processedAt(LocalDateTime.now())
                .build());

        log.info("OrderCancelled DLT retry başarılı — orderId: {}", event.orderId());
    }

    private void retryOrderConfirmed(DeadLetterEventEntity deadLetterEvent, String processedEventId) throws Exception {
        OrderConfirmedEvent event = objectMapper.readValue(deadLetterEvent.getPayload(), OrderConfirmedEvent.class);

        for (var item : event.items()) {
            productUseCase.confirmReservation(item.productId(), item.quantity());
        }

        processedEventRepository.save(ProcessedEventEntity.builder()
                .eventId(processedEventId)
                .eventType("OrderConfirmed-DLT-Retry")
                .processedAt(LocalDateTime.now())
                .build());

        log.info("OrderConfirmed DLT retry başarılı — orderId: {}", event.orderId());
    }
}