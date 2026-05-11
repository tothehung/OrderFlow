package com.orderflow.kafka;

import com.orderflow.event.OrderEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${app.kafka.topics.order-status-updated}")
    private String orderStatusUpdatedTopic;

    @Value("${app.kafka.topics.order-cancelled}")
    private String orderCancelledTopic;

    public void publishOrderCreated(OrderEvents.OrderCreated event) {
        send(orderCreatedTopic, event.getOrderId(), event);
    }

    public void publishOrderStatusUpdated(OrderEvents.OrderStatusUpdated event) {
        send(orderStatusUpdatedTopic, event.getOrderId(), event);
    }

    public void publishOrderCancelled(OrderEvents.OrderCancelled event) {
        send(orderCancelledTopic, event.getOrderId(), event);
    }

    private void send(String topic, String key, Object payload) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Published event to topic={} key={} offset={}",
                        topic, key, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event to topic={} key={}: {}", topic, key, ex.getMessage(), ex);
            }
        });
    }
}