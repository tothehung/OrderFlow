package com.orderflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.orderflow.websocket.OrderWebSocketHandler;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.socket.config.annotation.*;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.kafka.topics.order-created}")
    private String orderCreatedTopic;

    @Value("${app.kafka.topics.order-status-updated}")
    private String orderStatusUpdatedTopic;

    @Value("${app.kafka.topics.order-cancelled}")
    private String orderCancelledTopic;

    // ─── JSON ────────────────────────────────────────────────────────────────

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ─── CORS ────────────────────────────────────────────────────────────────

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(frontendUrl)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    // ─── Kafka topics (auto-created if missing) ───────────────────────────────

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(orderCreatedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic orderStatusUpdatedTopic() {
        return TopicBuilder.name(orderStatusUpdatedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(orderCancelledTopic).partitions(3).replicas(1).build();
    }

    // ─── Redis cache ─────────────────────────────────────────────────────────

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("orders", config.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("order-stats", config.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}

// ─── WebSocket config (separate @Configuration class) ────────────────────────

@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {

    private final OrderWebSocketHandler handler;
    private final String frontendUrl;

    WebSocketConfig(OrderWebSocketHandler handler,
                    @Value("${app.frontend-url}") String frontendUrl) {
        this.handler = handler;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/orders")
                .setAllowedOrigins(frontendUrl);
    }
}