package com.example.fbwotjq.spring.integration.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class TestController {

    @Resource(name = "defaultOutboundRabbitTemplate") RabbitTemplate defaultOutboundRabbitTemplate;

    @GetMapping(path = "/test/message")
    @ResponseBody public Map<String, Object> addTestMessage(long messageId) {

        log.info("[TestController] addTestMessage call...");

        Invoice invoice = new Invoice();
        invoice.setAccountId(messageId);
        invoice.setAdId((long) 1234);
        invoice.setAdAccountId((long) 1234);
        invoice.setAdRewardCreditId((long) 1234);
        invoice.setMode("TEST");
        invoice.setTalkUserId((long) 1234);
        invoice.setRetryCount(0);

        defaultOutboundRabbitTemplate.convertAndSend(invoice);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");

        return result;

    }

}
