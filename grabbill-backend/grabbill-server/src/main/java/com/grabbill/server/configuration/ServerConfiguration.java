package com.grabbill.server.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.zxing.qrcode.QRCodeWriter;
import com.grabbill.core.service.GrabbillSmtpService;
import com.grabbill.core.service.SmtpService;
import com.grabbill.server.service.*;
import com.warrenstrange.googleauth.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.util.concurrent.TimeUnit;

/**
 * @author michaellow
 */
@Configuration
public class ServerConfiguration {

    @Bean
    public AuthService authService() {
        return new AuthServiceImpl();
    }

    @Bean
    public FcmAuthService fcmAuthService() {
        return new FcmAuthServiceImpl();
    }

    @Bean
    public SmtpService smtpService() {
        return new GrabbillSmtpService();
    }

    @Bean
    public UserAccountEmailService userAccountEmailService() {
        return new UserAccountEmailServiceImpl();
    }

    @Bean
    UserRegistrationService userRegistrationService() {
        return new UserRegistrationServiceImpl();
    }

    @Bean
    PaymentNotificationEmailService paymentNotificationEmailService() {
        return new PaymentNotificationEmailServiceImpl();
    }

    @Bean
    AccountPaymentCheckService accountPaymentCheckService() {
        return new AccountPaymentCheckServiceImpl();
    }

    @Bean
    PlanSwitcherService planSwitcherService() {
        return new PlanSwitcherServiceImpl();
    }

    @Bean
    DigitalFilingReportService digitalFilingReportService() {
        return new DigitalFilingReportServiceImpl();
    }

    @Bean
    EmailCampaignReportService emailCampaignReportService() {
        return new EmailCampaignReportServiceImpl();
    }

    @Bean
    TransactionalEmailReportService transactionalEmailReportService() {
        return new TransactionalEmailReportServiceImpl();
    }

    @Bean
    MTTransactionalEmailReportService mtTransactionalEmailReportService() {
        return new MTTransactionalEmailReportServiceImpl();
    }

    @Bean
    SmsReportService smsReportService() {
        return new SmsReportServiceImpl();
    }

    @Bean
    InvoiceReportService invoiceReportService() {
        return new InvoiceReportServiceImpl();
    }

    @Bean
    AccountReportService accountReportService() {
        return new AccountReportServiceImpl();
    }

    @Bean
    WhatsAppReportService whatsAppReportService() {
        return new WhatsAppReportServiceImpl();
    }

    @Bean
    MTWhatsAppReportService mtWhatsAppReportService() {
        return new MTWhatsAppReportServiceImpl();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }

    @Bean
    @Qualifier("userGoogleAuthenticatorConfig")
    public GoogleAuthenticatorConfig userGoogleAuthenticatorConfig() {
        return new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))
                .setWindowSize(3)
                .setCodeDigits(6)
                .setNumberOfScratchCodes(0)
                .setSecretBits(128)
                .setKeyRepresentation(KeyRepresentation.BASE32)
                .setHmacHashFunction(HmacHashFunction.HmacSHA1)
                .build();
    }

    @Bean
    @Qualifier("userCredentialRepository")
    public ICredentialRepository userCredentialRepository() {
        return new UserCredentialRepository();
    }

    @Bean
    @Qualifier("userGoogleAuthenticator")
    public GoogleAuthenticator userGoogleAuthenticator() {
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator(userGoogleAuthenticatorConfig());
        googleAuthenticator.setCredentialRepository(userCredentialRepository());

        return googleAuthenticator;
    }

    @Bean
    @Qualifier("adminUserGoogleAuthenticatorConfig")
    public GoogleAuthenticatorConfig adminUserGoogleAuthenticatorConfig() {
        return new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))
                .setWindowSize(3)
                .setCodeDigits(6)
                .setNumberOfScratchCodes(0)
                .setSecretBits(128)
                .setKeyRepresentation(KeyRepresentation.BASE32)
                .setHmacHashFunction(HmacHashFunction.HmacSHA1)
                .build();
    }

    @Bean
    @Qualifier("adminUserCredentialRepository")
    public ICredentialRepository adminUserCredentialRepository() {
        return new AdminUserCredentialRepository();
    }

    @Bean
    @Qualifier("adminUserGoogleAuthenticator")
    public GoogleAuthenticator adminUserGoogleAuthenticator() {
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator(adminUserGoogleAuthenticatorConfig());
        googleAuthenticator.setCredentialRepository(adminUserCredentialRepository());

        return googleAuthenticator;
    }

    @Bean
    @Qualifier("userEmailAuthenticator")
    public EmailAuthenticator userEmailAuthenticator() {
        return new UserEmailAuthenticator();
    }

    @Bean
    @Qualifier("adminUserEmailAuthenticator")
    public EmailAuthenticator adminUserEmailAuthenticator() {
        return new AdminUserEmailAuthenticator();
    }

    @Bean
    public QRCodeWriter qrCodeWriter() {
        return new QRCodeWriter();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .serializationInclusion(JsonInclude.Include.ALWAYS)
                .serializationInclusion(JsonInclude.Include.NON_EMPTY)
                .serializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
