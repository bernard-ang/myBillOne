package com.grabbill.server.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author michaellow
 */
@Getter
public class JobProperties {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private int port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${job.queue.name}")
    private String queueName;

    @Value("${job.exchange.topic.name}")
    private String exchangeName;

    @Value("${payment-job.queue.name}")
    private String paymentJobQueueName;

    @Value("${payment-job.exchange.topic.name}")
    private String paymentJobExchangeName;

}
