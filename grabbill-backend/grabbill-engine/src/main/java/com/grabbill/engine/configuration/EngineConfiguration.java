package com.grabbill.engine.configuration;

import com.grabbill.core.service.CustomImapService;
import com.grabbill.core.service.ImapService;
import com.grabbill.engine.job.dsn.*;
import com.grabbill.engine.job.core.*;
import com.grabbill.core.service.SmtpService;
import com.grabbill.core.service.GrabbillSmtpService;
import com.grabbill.engine.service.*;
import com.grabbill.engine.service.sms.SmsProperties;
import com.grabbill.engine.service.sms.SmsService;
import com.grabbill.engine.service.sms.SmsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author michaellow
 */
@Configuration
public class EngineConfiguration {

    @Bean
    public SmtpService smtpService() {
        return new GrabbillSmtpService();
    }

    @Bean
    public ImapService imapService() {
        return new CustomImapService();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SmsProperties smsProperties() {
        return new SmsProperties();
    }

    @Bean
    public SmsService smsService() {
        return new SmsServiceImpl(smsProperties(), restTemplate());
    }

    @Bean
    public DsnBodyPartHelper dsnBodyPartHelper() {
        return new DsnBodyPartHelperImpl();
    }

    @Bean
    public DigitalFilingJobProcessor digitalFilingJobProcessor() {
        return new DigitalFilingJobProcessor();
    }

    @Bean
    public EmailCampaignJobProcessor emailCampaignJobProcessor() {
        return new EmailCampaignJobProcessor();
    }

    @Bean
    public TransactionalEmailJobProcessor transactionalEmailJobProcessor() {
        return new TransactionalEmailJobProcessor();
    }

    @Bean
    public MTTransactionalEmailJobProcessor mtTransactionalEmailJobProcessor() {
        return new MTTransactionalEmailJobProcessor();
    }

    @Bean
    public WhatsAppJobProcessor whatsAppJobProcessor() {
        return new WhatsAppJobProcessor();
    }

    @Bean
    public MTWhatsAppJobProcessor mtWhatsAppJobProcessor() {
        return new MTWhatsAppJobProcessor();
    }

    @Bean
    public SmsJobProcessor smsJobProcessor() {
        return new SmsJobProcessor();
    }

    @Bean
    public DsnScanJobProcessor dsnScanJobProcessor() {
        return new DsnScanJobProcessorImpl();
    }

    @Bean
    public SubscriptionManager subscriptionManager() {
        return new SubscriptionManagerImpl();
    }

    @Bean
    public JobEventConsumer jobEventConsumer() {
        return new JobEventConsumerImpl();
    }

    @Bean
    public DsnScanJobEventPublisher dsnScanJobEventPublisher() {
        return new DsnScanJobEventPublisherImpl();
    }

    @Bean
    public DsnScanJobEventConsumer dsnScanJobEventConsumer() {
        return new DsnScanJobEventConsumerImpl();
    }

    @Bean
    public SubscriptionPaymentNotificationEmailService subscriptionPaymentNotificationEmailService() {
        return new SubscriptionPaymentNotificationEmailServiceImpl();
    }

}
