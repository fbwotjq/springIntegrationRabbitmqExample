package com.example.fbwotjq.spring.integration.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PropertiesConfiguration {

    @Bean(name = "rabbitMQConditionerDefaultQueueProperties")
    @ConfigurationProperties(prefix = "rabbitmq.queue.conditioner.queue.default")
    public RabbitMQAppProperties rabbitMQDefaultConditionerQueueProperties() {
        return new RabbitMQAppProperties();
    }

    @Bean(name = "rabbitMQConditionerDefaultXDeadLetterQueueProperties")
    @ConfigurationProperties(prefix = "rabbitmq.queue.conditioner.queue.default-x-dead-letter")
    public RabbitMQAppProperties rabbitMQConditionerDefaultXDeadLetterQueueProperties() {
        return new RabbitMQAppProperties();
    }

}
