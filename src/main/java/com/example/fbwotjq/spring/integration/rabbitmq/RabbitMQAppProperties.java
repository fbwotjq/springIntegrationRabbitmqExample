package com.example.fbwotjq.spring.integration.rabbitmq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties
public class RabbitMQAppProperties {

    private String queueName;
    private int consumerCount;
    private Map<String, Object> args;
    private boolean durable;

}
