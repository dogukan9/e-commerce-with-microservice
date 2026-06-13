package com.shopwise.order.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwise.order.application.port.OrderUseCase;
import com.shopwise.order.infrastructure.kafka.event.StockReservationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationFailedEventConsumer {

    private final OrderUseCase orderUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "stock-reservation-failed", groupId = "order-service")
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String eventId = record.topic() + ":" + record.key();
        String payload = record.value();

        try {
            // Tek readValue
            String actualPayload = objectMapper.readValue(payload, String.class);
            StockReservationFailedEvent event = objectMapper.readValue(actualPayload, StockReservationFailedEvent.class);

            log.info("StockReservationFailed event alındı — orderId: {}", event.orderId());
            orderUseCase.cancelOrder(event.orderId());
            log.info("Sipariş iptal edildi — orderId: {}", event.orderId());

            ack.acknowledge();

        } catch (Exception e) {
            log.error("StockReservationFailed event işlenemedi — hata: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
