package com.orderflow.kafka;

import com.orderflow.event.OrderEvents;
import com.orderflow.websocket.OrderWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderWebSocketHandler webSocketHandler;

    @KafkaListener(
            topics = "${app.kafka.topics.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderCreated(
            @Payload OrderEvents.OrderCreated event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack
    ) {
        try {
            log.info("Consumed ORDER_CREATED event: orderId={} partition={} offset={}",
                    event.getOrderId(), partition, offset);

            // Broadcast real-time update to WebSocket clients
            webSocketHandler.broadcastOrderUpdate(event.getOrderId(), "ORDER_CREATED", event);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing ORDER_CREATED event: orderId={}", event.getOrderId(), e);
            // Don't ack — message goes back for retry
        }
    }

    @KafkaListener(
            topics = "${app.kafka.topics.order-status-updated}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderStatusUpdated(
            @Payload OrderEvents.OrderStatusUpdated event,
            Acknowledgment ack
    ) {
        try {
            log.info("Consumed ORDER_STATUS_UPDATED: orderId={} {} -> {}",
                    event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());

            webSocketHandler.broadcastOrderUpdate(event.getOrderId(), "ORDER_STATUS_UPDATED", event);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing ORDER_STATUS_UPDATED event: orderId={}", event.getOrderId(), e);
        }
    }

    @KafkaListener(
            topics = "${app.kafka.topics.order-cancelled}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderCancelled(
            @Payload OrderEvents.OrderCancelled event,
            Acknowledgment ack
    ) {
        try {
            log.info("Consumed ORDER_CANCELLED: orderId={}", event.getOrderId());

            webSocketHandler.broadcastOrderUpdate(event.getOrderId(), "ORDER_CANCELLED", event);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing ORDER_CANCELLED event: orderId={}", event.getOrderId(), e);
        }
    }
}