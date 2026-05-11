package com.orderflow.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // orderId -> list of sessions watching that order
    private final Map<String, CopyOnWriteArrayList<WebSocketSession>> orderSubscriptions = new ConcurrentHashMap<>();
    // sessionId -> set of subscribed orderIds (for cleanup on disconnect)
    private final Map<String, String> sessionOrderMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connected: sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Client sends: { "type": "SUBSCRIBE", "orderId": "xxx" }
        try {
            Map<?, ?> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) payload.get("type");
            String orderId = (String) payload.get("orderId");

            if ("SUBSCRIBE".equals(type) && orderId != null) {
                orderSubscriptions.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(session);
                sessionOrderMap.put(session.getId(), orderId);
                log.info("Session {} subscribed to order {}", session.getId(), orderId);

                // Ack subscription
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        Map.of("type", "SUBSCRIBED", "orderId", orderId))));
            }
        } catch (Exception e) {
            log.warn("Invalid WebSocket message from {}: {}", session.getId(), message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String orderId = sessionOrderMap.remove(session.getId());
        if (orderId != null) {
            CopyOnWriteArrayList<WebSocketSession> sessions = orderSubscriptions.get(orderId);
            if (sessions != null) sessions.remove(session);
        }
        log.info("WebSocket disconnected: sessionId={}", session.getId());
    }

    /**
     * Broadcasts an order event to all sessions subscribed to that orderId,
     * and also to "all orders" subscribers (for the dashboard list).
     */
    public void broadcastOrderUpdate(String orderId, String eventType, Object data) {
        Map<String, Object> message = Map.of(
                "type", eventType,
                "orderId", orderId,
                "data", data
        );

        try {
            String json = objectMapper.writeValueAsString(message);

            // Send to order-specific subscribers
            CopyOnWriteArrayList<WebSocketSession> sessions = orderSubscriptions.get(orderId);
            if (sessions != null) {
                sessions.removeIf(s -> !s.isOpen());
                for (WebSocketSession session : sessions) {
                    sendSafe(session, json);
                }
            }

            // Send to "all" subscribers (dashboard)
            CopyOnWriteArrayList<WebSocketSession> allSessions = orderSubscriptions.get("*");
            if (allSessions != null) {
                allSessions.removeIf(s -> !s.isOpen());
                for (WebSocketSession session : allSessions) {
                    sendSafe(session, json);
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize WebSocket broadcast for orderId={}", orderId, e);
        }
    }

    private void sendSafe(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            }
        } catch (IOException e) {
            log.warn("Failed to send WebSocket message to session={}", session.getId());
        }
    }
}