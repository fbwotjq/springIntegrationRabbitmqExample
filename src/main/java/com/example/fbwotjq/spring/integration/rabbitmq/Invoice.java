package com.example.fbwotjq.spring.integration.rabbitmq;

import lombok.Data;

@Data
public class Invoice {

    private Long adId;
    private Long talkUserId;
    private Long accountId;
    private Long adRewardCreditId;
    private Long adAccountId;
    private String mode;
    private Integer retryCount;

}
