package com.example.fbwotjq.spring.integration.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import javax.annotation.Resource;

@Slf4j
@Configuration
@EnableIntegration
@EnableRabbit
@IntegrationComponentScan("com.example.fbwotjq.spring.integration.rabbitmq")
public class FlowConfiguration {

    @Resource(name = "defaultAmqpInboundChannelAdapter") AmqpInboundChannelAdapter defaultAmqpInboundChannelAdapter;

    @Autowired ExampleHandler exampleHandler;

    @Bean("richInvoiceProcessFlow")
    public IntegrationFlow richInvoiceProcessFlow() {

        return IntegrationFlows.from(defaultAmqpInboundChannelAdapter)
            .log()
            .handle(exampleHandler)
            .get();

    }

}
