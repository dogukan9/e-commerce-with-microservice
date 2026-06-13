package com.shopwise.order.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwise.order.domain.model.Order;
import com.shopwise.order.domain.model.OrderStatus;
import com.shopwise.order.domain.port.OrderRepositoryPort;
import com.shopwise.order.infrastructure.exception.BusinessException;
import com.shopwise.order.infrastructure.exception.ErrorCode;
import com.shopwise.order.infrastructure.kafka.event.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservedEventConsumer {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "stock-reserved", groupId = "order-service")
    @Transactional
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String eventId = record.topic() + ":" + record.key();
        String payload = record.value();

        log.info("StockReserved event alındı — eventId: {}", eventId);

        try {
            // Tek readValue — ProductEventPublisher direkt JSON gönderiyor
// Tek readValue yetmez, iki kez lazım
            String actualPayload = objectMapper.readValue(payload, String.class);
            StockReservedEvent event = objectMapper.readValue(actualPayload, StockReservedEvent.class);
            Order order = orderRepositoryPort.findById(event.orderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND,
                            "Sipariş bulunamadı: " + event.orderId()));

            if (order.getStatus().ordinal() > OrderStatus.PENDING.ordinal()) {
                log.warn("Sipariş zaten işlendi, atlanıyor — orderId: {}", event.orderId());
                ack.acknowledge();
                return;
            }

            order.markStockReserved();
            orderRepositoryPort.save(order);

            ack.acknowledge();
            log.info("Sipariş STOCK_RESERVED yapıldı — orderId: {}", event.orderId());

        } catch (Exception e) {
            log.error("StockReserved event işlenemedi — hata: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }}
