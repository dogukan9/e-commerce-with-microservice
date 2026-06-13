package com.shopwise.order.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwise.order.application.port.OrderUseCase;
import com.shopwise.order.domain.model.Order;
import com.shopwise.order.domain.port.OrderRepositoryPort;
import com.shopwise.order.infrastructure.exception.BusinessException;
import com.shopwise.order.infrastructure.exception.ErrorCode;
import com.shopwise.order.infrastructure.kafka.event.StockReservationFailedEvent;
import com.shopwise.order.infrastructure.kafka.event.StockReservedEvent;
import com.shopwise.order.infrastructure.persistence.DeadLetterEventEntity;
import com.shopwise.order.infrastructure.persistence.DeadLetterEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterRetryService {

    private final DeadLetterEventRepository deadLetterEventRepository;
    private final OrderUseCase orderUseCase;
    private final OrderRepositoryPort orderRepositoryPort;
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

        try {
            switch (originalTopic) {
                case "stock-reservation-failed" -> retryStockReservationFailed(deadLetterEvent);
                case "stock-reserved" -> retryStockReserved(deadLetterEvent);
                default -> log.error("Bilinmeyen topic: {}", originalTopic);
            }

            deadLetterEvent.setResolved(true);
            deadLetterEvent.setResolvedAt(LocalDateTime.now());
            deadLetterEventRepository.save(deadLetterEvent);

            log.info("Dead letter event çözüldü — id: {}", deadLetterEventId);

        } catch (Exception e) {
            log.error("Dead letter retry başarısız — id: {}, hata: {}",
                    deadLetterEventId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void retryStockReservationFailed(DeadLetterEventEntity deadLetterEvent) throws Exception {
        StockReservationFailedEvent event = objectMapper.readValue(
                deadLetterEvent.getPayload(), StockReservationFailedEvent.class);

        orderUseCase.cancelOrder(event.orderId());

        log.info("StockReservationFailed DLT retry başarılı — orderId: {}", event.orderId());
    }

    private void retryStockReserved(DeadLetterEventEntity deadLetterEvent) throws Exception {
        StockReservedEvent event = objectMapper.readValue(
                deadLetterEvent.getPayload(), StockReservedEvent.class);

        Order order = orderRepositoryPort.findById(event.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                        "Sipariş bulunamadı: " + event.orderId()));

        // Sipariş zaten işlendiyse atla
        if (order.getStatus().ordinal() > com.shopwise.order.domain.model.OrderStatus.PENDING.ordinal()) {
            log.warn("Sipariş zaten işlendi, atlanıyor — orderId: {}", event.orderId());
            return;
        }

        order.markStockReserved();
        orderRepositoryPort.save(order);

        log.info("StockReserved DLT retry başarılı — orderId: {}", event.orderId());
    }
}