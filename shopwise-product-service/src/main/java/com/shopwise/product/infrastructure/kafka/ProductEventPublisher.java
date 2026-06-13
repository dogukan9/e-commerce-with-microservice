package com.shopwise.product.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwise.product.infrastructure.kafka.event.StockReservationFailedEvent;
import com.shopwise.product.infrastructure.kafka.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishStockReserved(StockReservedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("stock-reserved", String.valueOf(event.orderId()), payload);
            log.info("StockReserved event yayınlandı — orderId: {}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("StockReserved event yayınlanamadı: {}", e.getMessage());
        }
    }

    public void publishStockReservationFailed(StockReservationFailedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("stock-reservation-failed", String.valueOf(event.orderId()), payload);
            log.info("StockReservationFailed event yayınlandı — orderId: {}", event.orderId());
        } catch (JsonProcessingException e) {
            log.error("StockReservationFailed event yayınlanamadı: {}", e.getMessage());
        }
    }
}
