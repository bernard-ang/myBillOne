package com.grabbill.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabbill.core.entity.*;
import com.grabbill.core.service.*;
import com.grabbill.core.service.payment.*;
import com.grabbill.core.service.whatsapp.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author michaellow
 */
@Configuration
@EnableJpaRepositories("com.grabbill.core.repository")
@EntityScan("com.grabbill.core.entity")
public class CoreConfiguration {

    @Bean
    UserService userService() {
        return new UserServiceImpl();
    }

    @Bean
    AdminUserService adminUserService() {
        return new AdminUserServiceImpl();
    }

    @Bean
    AccountService accountService() {
        return new AccountServiceImpl();
    }

    @Bean
    AccountSubscriptionService accountSubscriptionService() {
        return new AccountSubscriptionServiceImpl();
    }

    @Bean
    AccountUsageStatisticService accountUsageStatisticService() {
        return new AccountUsageStatisticServiceImpl();
    }

    @Bean
    AccountStatementService accountStatementService() {
        return new AccountStatementServiceImpl();
    }

    @Bean
    PrivilegeService privilegeService() {
        return new PrivilegeServiceImpl();
    }

    @Bean
    RoleService roleService() {
        return new RoleServiceImpl();
    }

    @Bean
    PlanService planService() {
        return new PlanServiceImpl();
    }

    @Bean
    FileServerProperties fileServerProperties() {
        return new FileServerPropertiesImpl();
    }

    @Bean
    FileStorageService fileStorageService() {
        return new FileStorageServiceImpl(fileServerProperties());
    }

    @Bean
    JobService jobService() {
        return new JobServiceImpl();
    }

    @Bean
    AdminAuditLogService adminAuditLogService() {
        return new AdminAuditLogServiceImpl();
    }

    @Bean
    AuditLogService auditLogService() {
        return new AuditLogServiceImpl();
    }

    @Bean
    MailServerService mailServerService() {
        return new MailServerServiceImpl();
    }

    @Bean
    UnsubscribedEmailService unsubscribedEmailService() {
        return new UnsubscribedEmailServiceImpl();
    }

    @Bean
    UnsubscribedEmailLinkService unsubscribedEmailLinkService() {
        return new UnsubscribedEmailLinkServiceImpl();
    }

    @Bean
    BouncedEmailService bouncedEmailService() {
        return new BouncedEmailServiceImpl();
    }

    @Bean
    MessageIdGenerator messageIdGenerator() {
        return new MessageIdGeneratorImpl();
    }

    @Bean
    IndexRowHelper indexRowHelper() {
        return new IndexRowHelperImpl();
    }

    @Bean
    ImageFolderService imageFolderService() {
        return new ImageFolderServiceImpl();
    }

    @Bean
    ImageService imageService() {
        return new ImageServiceImpl();
    }

    @Bean
    PlanUsageService planUsageService() {
        return new PlanUsageServiceImpl();
    }

    @Bean
    @Qualifier("digitalFilingTypeService")
    BaseTypeService<DigitalFilingType, DigitalFilingActivity> digitalFilingTypeService() {
        return new DigitalFilingTypeServiceImpl();
    }

    @Bean
    @Qualifier("digitalFilingActivityService")
    BaseActivityService<DigitalFilingType, DigitalFilingActivity> digitalFilingActivityService() {
        return new DigitalFilingActivityServiceImpl();
    }

    @Bean
    @Qualifier("digitalFilingFileService")
    BaseFileService<DigitalFilingType, DigitalFilingActivity, DigitalFilingFile> digitalFilingFileService() {
        return new DigitalFilingFileServiceImpl();
    }

    @Bean
    @Qualifier("digitalFilingIndexRowService")
    BaseIndexRowService<DigitalFilingType, DigitalFilingActivity, DigitalFilingIndexRow> digitalFilingIndexRowService() {
        return new DigitalFilingIndexRowServiceImpl();
    }

    @Bean
    @Qualifier("digitalFilingRecordService")
    BaseRecordService<DigitalFilingRecord> digitalFilingRecordService() {
        return new DigitalFilingRecordServiceImpl();
    }

    @Bean
    @Qualifier("digitalFilingActivitySftpService")
    BaseActivitySftpService<DigitalFilingType, DigitalFilingActivity, DigitalFilingIndexField> digitalFilingActivitySftpService() {
        return new DigitalFilingActivitySftpServiceImpl();
    }

    @Bean
    @Qualifier("transactionalEmailTypeService")
    BaseTypeService<TransactionalEmailType, TransactionalEmailActivity> transactionalEmailTypeService() {
        return new TransactionalEmailTypeServiceImpl();
    }

    @Bean
    @Qualifier("transactionalEmailActivityService")
    BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> transactionalEmailActivityService() {
        return new TransactionalEmailActivityServiceImpl();
    }

    @Bean
    @Qualifier("transactionalEmailFileService")
    BaseFileService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailFile> transactionalEmailFileService() {
        return new TransactionalEmailFileServiceImpl();
    }

    @Bean
    @Qualifier("transactionalEmailIndexRowService")
    BaseIndexRowService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailIndexRow> transactionalEmailIndexRowService() {
        return new TransactionalEmailIndexRowServiceImpl();
    }

    @Bean
    @Qualifier("transactionalEmailRecordService")
    TransactionalEmailRecordService transactionalEmailRecordService() {
        return new TransactionalEmailRecordServiceImpl();
    }

    @Bean
    ContactService contactService() {
        return new ContactServiceImpl();
    }

    @Bean
    ContactFieldService contactFieldService() {
        return new ContactFieldServiceImpl();
    }

    @Bean
    ContactGroupService contactGroupService() {
        return new ContactGroupServiceImpl();
    }

    @Bean
    @Qualifier("emailCampaignTypeService")
    BaseTypeService<EmailCampaignType, EmailCampaignActivity> emailCampaignTypeService() {
        return new EmailCampaignTypeServiceImpl();
    }

    @Bean
    @Qualifier("emailCampaignActivityService")
    BaseActivityService<EmailCampaignType, EmailCampaignActivity> emailCampaignActivityService() {
        return new EmailCampaignActivityServiceImpl();
    }

    @Bean
    @Qualifier("emailCampaignFileService")
    BaseFileService<EmailCampaignType, EmailCampaignActivity, EmailCampaignFile> emailCampaignFileService() {
        return new EmailCampaignFileServiceImpl();
    }

    @Bean
    @Qualifier("emailCampaignIndexRowService")
    BaseIndexRowService<EmailCampaignType, EmailCampaignActivity, EmailCampaignIndexRow> emailCampaignIndexRowService() {
        return new EmailCampaignIndexRowServiceImpl();
    }

    @Bean
    @Qualifier("emailCampaignRecordService")
    BaseRecordService<EmailCampaignRecord> emailCampaignRecordService() {
        return new EmailCampaignRecordServiceImpl();
    }

    @Bean
    @Qualifier("smsTypeService")
    BaseTypeService<SmsType, SmsActivity> smsTypeService() {
        return new SmsTypeServiceImpl();
    }

    @Bean
    @Qualifier("smsActivityService")
    BaseActivityService<SmsType, SmsActivity> smsActivityService() {
        return new SmsActivityServiceImpl();
    }

    @Bean
    @Qualifier("smsIndexRowService")
    BaseIndexRowService<SmsType, SmsActivity, SmsIndexRow> smsIndexRowService() {
        return new SmsIndexRowServiceImpl();
    }

    @Bean
    @Qualifier("smsRecordService")
    BaseRecordService<SmsRecord> smsRecordService() {
        return new SmsRecordServiceImpl();
    }

    @Bean
    TaxService taxService() {
        return new TaxServiceImpl();
    }

    @Bean
    CustomerService customerService() {
        return new CustomerServiceImpl();
    }

    @Bean
    CustomerPaymentMethodService customerPaymentMethodService() {
        return new CustomerPaymentMethodServiceImpl();
    }

    @Bean
    PaymentService paymentService() {
        return new PaymentServiceImpl();
    }

    @Bean
    ProductService productService() {
        return new ProductServiceImpl();
    }

    @Bean
    InvoiceService invoiceService() {
        return new InvoiceServiceImpl();
    }

    @Bean
    ProRateCalculator proRatedCalculator() {
        return new ProRateCalculatorImpl();
    }

    @Bean
    AESService aesService() {
        return new AESServiceImpl();
    }

    @Bean
    EmbeddedLinkService embeddedLinkService() {
        return new EmbeddedLinkServiceImpl();
    }

    @Bean
    EmbeddedLinkClickService embeddedLinkClickService() {
        return new EmbeddedLinkClickServiceImpl();
    }

    @Bean
    AffiliateCodeService affiliateCodeService() {
        return new AffiliateCodeServiceImpl();
    }

    @Bean
    StripeEventService stripeEventService() {
        return new StripeEventServiceImpl();
    }

    @Bean
    PromoCodeService promoCodeService() {
        return new PromoCodeServiceImpl();
    }

    @Bean
    CreditsPlanOptionService creditsPlanOptionService() {
        return new CreditsPlanOptionServiceImpl();
    }

    @Bean
    public WhatsAppService whatsAppService(ObjectMapper mapper) {
        return new WhatsAppService(mapper);
    }

    @Bean
    public WhatsAppEventService whatsAppEventService() {
        return new WhatsAppEventServiceImpl();
    }

    @Bean
    public WhatsAppEventManager whatsAppEventManager() {
        return new WhatsAppManagerImpl();
    }

    @Bean
    @Qualifier("whatsAppTypeService")
    BaseTypeService<WhatsAppType, WhatsAppActivity> whatsAppTypeService() {
        return new WhatsAppTypeServiceImpl();
    }

    @Bean
    @Qualifier("whatsAppActivityService")
    BaseActivityService<WhatsAppType, WhatsAppActivity> whatsAppActivityService() {
        return new WhatsAppActivityServiceImpl();
    }

    @Bean
    @Qualifier("whatsAppFileService")
    BaseFileService<WhatsAppType, WhatsAppActivity, WhatsAppFile> whatsAppFileService() {
        return new WhatsAppFileServiceImpl();
    }

    @Bean
    @Qualifier("whatsAppIndexRowService")
    BaseIndexRowService<WhatsAppType, WhatsAppActivity, WhatsAppIndexRow> whatsAppIndexRowService() {
        return new WhatsAppIndexRowServiceImpl();
    }

    @Bean
    @Qualifier("whatsAppRecordService")
    WhatsAppRecordService whatsAppRecordService() {
        return new WhatsAppRecordServiceImpl();
    }

    @Bean
    @Qualifier("mtWhatsAppTypeService")
    BaseTypeService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppTypeService() {
        return new MTWhatsAppTypeServiceImpl();
    }

    @Bean
    @Qualifier("mtWhatsAppActivityService")
    BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService() {
        return new MTWhatsAppActivityServiceImpl();
    }

    @Bean
    @Qualifier("mtWhatsAppFileService")
    BaseFileService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppFile> mtWhatsAppFileService() {
        return new MTWhatsAppFileServiceImpl();
    }

    @Bean
    @Qualifier("mtWhatsAppIndexRowService")
    BaseIndexRowService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppIndexRow> mtWhatsAppIndexRowService() {
        return new MTWhatsAppIndexRowServiceImpl();
    }

    @Bean
    @Qualifier("mtWhatsAppRecordService")
    MTWhatsAppRecordService mtWhatsAppRecordService() {
        return new MTWhatsAppRecordServiceImpl();
    }

    @Bean
    @Qualifier("mtWhatsAppActivitySftpService")
    BaseActivitySftpService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppIndexField> mtWhatsappActivitySftpService() {
        return new MTWhatsappActivitySftpServiceImpl();
    }

    @Bean
    SftpService sftpService() {
        return new SftpServiceImpl();
    }

    @Bean
    @Qualifier("mtTransactionalEmailTypeService")
    BaseTypeService<MTTransactionalEmailType, MTTransactionalEmailActivity> mtTransactionalEmailTypeService() {
        return new MTTransactionalEmailTypeServiceImpl();
    }

    @Bean
    @Qualifier("mtTransactionalEmailActivityService")
    BaseActivityService<MTTransactionalEmailType, MTTransactionalEmailActivity> mtTransactionalEmailActivityService() {
        return new MTTransactionalEmailActivityServiceImpl();
    }

    @Bean
    @Qualifier("mtTransactionalEmailFileService")
    BaseFileService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailFile> mtTransactionalEmailFileService() {
        return new MTTransactionalEmailFileServiceImpl();
    }

    @Bean
    @Qualifier("mtTransactionalEmailIndexRowService")
    BaseIndexRowService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailIndexRow> mtTransactionalEmailIndexRowService() {
        return new MTTransactionalEmailIndexRowServiceImpl();
    }

    @Bean
    @Qualifier("mtTransactionalEmailRecordService")
    MTTransactionalEmailRecordService mtTransactionalEmailRecordService() {
        return new MTTransactionalEmailRecordServiceImpl();
    }

    @Bean
    @Qualifier("mtTransactionalEmailActivitySftpService")
    BaseActivitySftpService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailIndexField> mtTransactionalEmailActivitySftpService() {
        return new MTTransactionalEmailActivitySftpServiceImpl();
    }

    @Bean
    ActivityTrackingService activityTrackingService(
            BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService,
            TransactionTemplate txTemplate) {
        return new ActivityTrackingServiceImpl(mtWhatsAppActivityService, txTemplate);
    }
}
