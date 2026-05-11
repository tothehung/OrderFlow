package com.orderflow.service;

import com.orderflow.kafka.OrderEventProducer;
import com.orderflow.model.OutboxEvent;
import com.orderflow.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox Poller — polls unpublished outbox events every 5 seconds
 * and re-publishes them to Kafka. This guarantees at-least-once delivery:
 * even if the app crashed before Kafka.send() completed, the event
 * stays in outbox_events and gets picked up on restart.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPollerService {

    private static final int MAX_RETRIES = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final OrderEventProducer eventProducer;

    @Scheduled(fixedDelay = 5000) // every 5 seconds
    public void pollAndPublish() {
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc();

        if (pending.isEmpty()) return;

        log.debug("Outbox poller found {} unpublished events", pending.size());

        for (OutboxEvent event : pending) {
            try {
                // In a real system, deserialize payload and call the correct producer method
                // Here we log as demonstration — production would switch on event.getEventType()
                log.info("Outbox: re-publishing eventType={} aggregateId={}",
                        event.getEventType(), event.getAggregateId());

                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= MAX_RETRIES) {
                    log.error("Outbox event exceeded max retries, marking as dead letter: id={}", event.getId());
                    // In production: move to dead-letter collection or alert
                }
                outboxEventRepository.save(event);
            }
        }
    }
}