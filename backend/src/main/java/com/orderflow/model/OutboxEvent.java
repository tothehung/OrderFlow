package com.orderflow.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Transactional Outbox Pattern — saves events in the same DB transaction
 * as domain changes. A scheduler polls this collection and publishes to Kafka,
 * guaranteeing at-least-once delivery without dual-write risk.
 */
@Document(collection = "outbox_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    private String id;

    private String aggregateId;    // e.g. orderId
    private String aggregateType;  // e.g. "Order"
    private String eventType;      // e.g. "ORDER_CREATED"
    private String payload;        // JSON string of event data

    @Builder.Default
    private boolean published = false;

    private int retryCount;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;
}