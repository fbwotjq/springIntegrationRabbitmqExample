package com.example.fbwotjq.spring.integration.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ExampleHandler implements GenericHandler<Invoice> {

    @Override
    public Object handle(Invoice payload, Map<String, Object> headers) {

        if(payload.getAccountId() == 1111) {

            throw new RuntimeException();

        }
        log.info(payload.toString());
        return null;

    }
}
