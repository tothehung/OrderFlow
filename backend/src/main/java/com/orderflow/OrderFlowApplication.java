package com.orderflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@SpringBootApplication
@EnableCaching
@EnableMongoAuditing
@EnableAsync
@EnableScheduling
public class OrderFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderFlowApplication.class, args);
    }


    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OrderFlow Platform API")
                        .version("1.0.0")
                        .description("Tài liệu API cho hệ thống quản lý đơn hàng Microservices"));
        }
    }   