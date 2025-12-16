package com.grabbill.engine.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author michaellow
 */
@Configuration
public class EngineJobQueueConfiguration {

    @Bean
    public JobProperties jobProperties() {
        return new JobProperties();
    }

    @Bean
    @Qualifier("jobQueue")
    public Queue jobQueue() {
        return new Queue(jobProperties().getQueueName(), true);
    }

    @Bean
    @Qualifier("dsnScanJobQueue")
    public Queue dsnScanJobQueue() {
        return new Queue(jobProperties().getDsnScanQueueName(), true);
    }

    @Bean
    @Qualifier("dsnScanJobExchange")
    DirectExchange dsnScanExchange() {
        return new DirectExchange(jobProperties().getDsnScanExchangeName(), true, false);
    }

    @Bean
    Binding bindingDsnScanJobQueue(@Qualifier("dsnScanJobExchange") final DirectExchange exchange) {
        return BindingBuilder
                .bind(dsnScanJobQueue())
                .to(exchange)
                .withQueueName();
    }

}
