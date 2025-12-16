package com.grabbill.server.configuration;

import com.grabbill.server.service.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author michaellow
 */
@Configuration
public class JobConfiguration {

    @Bean
    public JobProperties jobProperties() {
        return new JobProperties();
    }

    @Bean
    public ConnectionFactory connectionFactory(final JobProperties jobProperties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(jobProperties.getHost());
        connectionFactory.setPort(jobProperties.getPort());
        connectionFactory.setUsername(jobProperties.getUsername());
        connectionFactory.setPassword(jobProperties.getPassword());
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory(jobProperties()));
    }

    @Bean
    @Qualifier("jobQueue")
    public Queue jobQueue() {
        return new Queue(jobProperties().getQueueName(), true);
    }

    @Bean
    @Qualifier("jobExchange")
    DirectExchange jobExchange() {
        return new DirectExchange(jobProperties().getExchangeName(), true, false);
    }

    @Bean
    Binding bindingJobQueue(@Qualifier("jobExchange") final DirectExchange exchange) {
        return BindingBuilder
                .bind(jobQueue())
                .to(exchange)
                .withQueueName();
    }

    @Bean
    public JobScheduler jobScheduler() {
        return new JobSchedulerImpl();
    }

    @Bean
    public JobEventPublisher jobEventPublisher() {
        return new JobEventPublisherImpl();
    }

    @Bean
    public JobControllerService jobControllerService() {
        return new JobControllerServiceImpl();
    }

}
