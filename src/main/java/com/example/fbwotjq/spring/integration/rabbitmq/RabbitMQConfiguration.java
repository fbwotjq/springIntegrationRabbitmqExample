package com.example.fbwotjq.spring.integration.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.amqp.AmqpInboundChannelAdapterSpec;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Configuration
@EnableRabbit
public class RabbitMQConfiguration {

    private final static int rabbitConsummerThreadPoolArrayBlockingQueueSize = 1000;
    private final static int rabbitConsummerThreadPoolKeepAliveTime = 1000;

    @Resource(name = "rabbitMQConditionerDefaultQueueProperties")
    RabbitMQAppProperties rabbitMQDefaultConditionerQueueProperties;

    @Resource(name = "rabbitMQConditionerDefaultXDeadLetterQueueProperties")
    RabbitMQAppProperties rabbitMQConditionerDefaultXDeadLetterQueueProperties;

    @Bean(value = "rabbitMQConditionerDefaultExchange")
    DirectExchange rabbitMQConditionerDefaultExchange() {

        DirectExchange directExchange = (DirectExchange) ExchangeBuilder.directExchange("amq.direct").durable(true).build();
        return directExchange;

    }

    @Bean(value = "rabbitMQConditionerDefaultXDeadLetterQueue")
    Queue rabbitMQConditionerDefaultXDeadLetterQueue() {

        Queue queue = QueueBuilder.durable(rabbitMQConditionerDefaultXDeadLetterQueueProperties.getQueueName())
                //.withArguments(rabbitMQConditionerDefaultXDeadLetterQueueProperties.getArgs())
                .build();
        return queue;

    }

    @Bean(value = "rabbitMQConditionerDefaultQueue")
    Queue rabbitMQConditionerDefaultQueue() {

        Map<String, Object> args = rabbitMQDefaultConditionerQueueProperties.getArgs();
        args.put("x-dead-letter-exchange", "");
        args.put("x-message-ttl", 3000);

        Queue queue = QueueBuilder.durable(rabbitMQDefaultConditionerQueueProperties.getQueueName())
                .withArguments(args)
                .build();

        return queue;

    }


    @Bean(value = "rabbitMQConditionerDefaultBinding")
    Binding rabbitMQConditionerDefaultBinding() {

        String with = (String) rabbitMQDefaultConditionerQueueProperties.getArgs().get("x-dead-letter-routing-key");

        return BindingBuilder.bind(rabbitMQConditionerDefaultQueue())
            .to(rabbitMQConditionerDefaultExchange()).with(with);

    }

    @Bean(name = "defaultOutboundRabbitTemplate")
    public RabbitTemplate defaultOutboundRabbitTemplate(ConnectionFactory connectionFactory) {

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setRoutingKey(rabbitMQDefaultConditionerQueueProperties.getQueueName());
        return rabbitTemplate;

    }
    @Bean(value = "rabbitConsummerThreadPool")
    public ThreadPoolExecutor rabbitConsummerThreadPool() {

        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        ArrayBlockingQueue arrayBlockingQueue = new ArrayBlockingQueue<Runnable>(rabbitConsummerThreadPoolArrayBlockingQueueSize);
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                rabbitMQDefaultConditionerQueueProperties.getConsumerCount(),
                rabbitMQDefaultConditionerQueueProperties.getConsumerCount(),
                rabbitConsummerThreadPoolKeepAliveTime, TimeUnit.SECONDS, arrayBlockingQueue, threadFactory,
                (Runnable runnable, ThreadPoolExecutor poolExecutor) -> {
                    log.error("[THREAD-POOL-ERROR] rabbitConsummerThreadPool is rejected.. ");
                }
        );

        return threadPoolExecutor;

    }

    @Bean("inboundMessageConverter")
    public MessageConverter inboundMessageConverter() {

        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setClassMapper(new ClassMapper() {
            @Override
            public void fromClass(Class<?> aClass, MessageProperties messageProperties) {
                throw new UnsupportedOperationException("this mapper is only for inbound");
            }

            @Override
            public Class<?> toClass(MessageProperties messageProperties) {
                return Invoice.class;
            }
        });
        return jackson2JsonMessageConverter;

    }

    @Bean(name = "defaultAmqpInboundChannelAdapter")
    public AmqpInboundChannelAdapter defaultAmqpInboundChannelAdapterSpec(ConnectionFactory connectionFactory) throws Exception {

        RabbitMQAppProperties rabbitMQAppProperties = rabbitMQDefaultConditionerQueueProperties;

        log.info(String.format("[defaultAmqpInboundChannelAdapter] init => %s", rabbitMQAppProperties.toString()));

        AmqpInboundChannelAdapterSpec amqpInboundChannelAdapterSpec = Amqp.inboundAdapter(connectionFactory, rabbitMQAppProperties.getQueueName());
        amqpInboundChannelAdapterSpec.concurrentConsumers(rabbitMQAppProperties.getConsumerCount());
        amqpInboundChannelAdapterSpec.maxConcurrentConsumers(rabbitMQAppProperties.getConsumerCount());
        amqpInboundChannelAdapterSpec.taskExecutor(rabbitConsummerThreadPool());
        amqpInboundChannelAdapterSpec.messageConverter(inboundMessageConverter());
        // amqpInboundChannelAdapterSpec.acknowledgeMode(AcknowledgeMode.AUTO); // 기본이 AUTO 이며 실패시 DXL 기능을 이용하기에 AUTO로 사용
        // amqpInboundChannelAdapterSpec.prefetchCount(1); // spring amqp default prefetchCount가 1
        amqpInboundChannelAdapterSpec.errorHandler(throwable -> {
            log.error("[defaultAmqpInboundChannelAdapter] " + throwable.getMessage());
            log.error("[defaultAmqpInboundChannelAdapter] " + throwable.getCause().getMessage());
        });

        amqpInboundChannelAdapterSpec.autoStartup(true);
        AmqpInboundChannelAdapter amqpInboundChannelAdapter = amqpInboundChannelAdapterSpec.getObject();

        return amqpInboundChannelAdapter;

    }

}
