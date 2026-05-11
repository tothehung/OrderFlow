package com.orderflow.repository;

import com.orderflow.model.OutboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends MongoRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop50ByPublishedFalseOrderByCreatedAtAsc();
}