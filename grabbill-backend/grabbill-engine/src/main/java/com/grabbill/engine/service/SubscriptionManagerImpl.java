package com.grabbill.engine.service;

import com.grabbill.core.conf.DeploymentProperties;
import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.DiscountOccurrence;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.core.service.*;
import com.grabbill.core.service.payment.CustomerPaymentMethodService;
import com.grabbill.core.service.payment.CustomerService;
import com.grabbill.core.service.payment.PaymentService;
import com.stripe.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author michaellow
 */
@Slf4j
public class SubscriptionManagerImpl implements SubscriptionManager {

    @Value("${payment.test-mode:false}")
    private boolean paymentTestMode;

    @Autowired
    private DeploymentProperties deploymentProperties;

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;

    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    private AccountStatementService accountStatementService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerPaymentMethodService customerPaymentMethodService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PlanService planService;

    @Autowired
    private PromoCodeService promoCodeService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private SubscriptionPaymentNotificationEmailService subscriptionPaymentNotificationEmailService;


    @Transactional
    @Override
    public void process() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime then = now.plusDays(1);

        List<Plan> allPlans = planService.getAll();
        List<AccountSubscription> subscriptionsReadyForCycleRenew = accountSubscriptionService.getAllActiveSubscriptionsByCycleEndDateBefore(then);

        for (AccountSubscription accountSubscription : subscriptionsReadyForCycleRenew) {

            Account account = accountSubscription.getAccount();
            User accountOwner = getAccountOwner(account);

            Customer customer = customerService.getOrCreate(account);
            com.stripe.model.PaymentMethod defaultPaymentMethod = customerPaymentMethodService.getDefaultPaymentMethodByCustomerId(customer.getId());
            BillingInformation billingInformation = customerService.getBillingInformation(customer, defaultPaymentMethod);
            PromoCode promoCode = getPromoCode(accountSubscription);


            // 1. create account statement, reset plan usage statistic & renew plan usage statistic & cycle
            AccountUsageStatistic usageStatistic = accountUsageStatisticService.getOrCreateNew(account, accountSubscription);
            accountStatementService.newStatement(account, accountSubscription, usageStatistic);
            accountUsageStatisticService.resetByAccountId(account.getId());
            AccountSubscription renewedAccountSubscription = accountSubscriptionService.renewMonthlyUsageCycle(accountSubscription, now);


            // 2. create invoice & charge if payment is not exempted and deployment mode is not on-premise
            if ((SubscriptionMode.MONTHLY.equals(renewedAccountSubscription.getMode()))
                    || (SubscriptionMode.ANNUALLY.equals(renewedAccountSubscription.getMode())
                            && accountSubscriptionService.isMaturedForAnnualCharge(accountSubscription, now))
            ) {

                if (!account.isPaymentExempted() && !deploymentProperties.isOnPremise()) {

                    // 2.1. proceed to charge all unpaid invoices first (if exists)
                    processUnpaidInvoices(invoiceService.getUnpaidInvoices(account));

                    // 2.2. proceed to create new invoice and charge for upcoming payment cycle (for PAID plan)
                    if (accountSubscriptionService.getMonthlyRate(renewedAccountSubscription) > 0) {
                        Plan plan = getMatchingPlan(renewedAccountSubscription, allPlans);

                        if (plan != null) {

                            StoragePlanOption storageOption;
                            TransactionalEmailPlanOption transactionalEmailOption;
                            EmailCampaignPlanOption emailCampaignOption;

                            if (paymentTestMode) {
                                storageOption = new StoragePlanOption();
                                transactionalEmailOption = new TransactionalEmailPlanOption();
                                emailCampaignOption = new EmailCampaignPlanOption();
                                storageOption.setId(99L);
                                transactionalEmailOption.setId(99L);
                                emailCampaignOption.setId(99L);

                            } else {
                                storageOption = (StoragePlanOption) planService.getBasePlanOption(
                                        renewedAccountSubscription.getStorageSize(), plan.getStorageOptions(), "storage");
                                transactionalEmailOption = (TransactionalEmailPlanOption) planService.getBasePlanOption(
                                        renewedAccountSubscription.getTransactionalEmailSize(), plan.getTransactionalEmailOptions(), "transactional email");
                                emailCampaignOption = (EmailCampaignPlanOption) planService.getBasePlanOption(
                                        renewedAccountSubscription.getEmailCampaignSize(), plan.getEmailCampaignOptions(), "email campaign");
                            }

                            com.stripe.model.Invoice stripeInvoice = paymentService.chargeInvoice(
                                    paymentService.createInvoice(
                                            account,
                                            renewedAccountSubscription,
                                            !paymentTestMode ? plan.getId() : 99,
                                            renewedAccountSubscription.getMode(),
                                            storageOption,
                                            transactionalEmailOption,
                                            emailCampaignOption,
                                            0d,
                                            promoCode
                                    ).getId()
                            );

                            // create new invoice for coming cycle and proceed to charge
                            Invoice targetInvoice = invoiceService.createNewPersistent(
                                    accountOwner,
                                    stripeInvoice,
                                    billingInformation,
                                    account,
                                    renewedAccountSubscription,
                                    0,
                                    promoCode
                            );

                            subscriptionPaymentNotificationEmailService.sendEmail(targetInvoice, stripeInvoice);

                        } else {
                            log.warn("No matching plan");
                        }

                    } else {
                        log.debug("Free plan");
                    }
                }
            }
        }
    }

    private void processUnpaidInvoices(final List<Invoice> unpaidInvoices) {
        for (Invoice unpaidInvoice : unpaidInvoices) {

            com.stripe.model.Invoice stripeInvoice;
            if (StringUtils.hasLength(unpaidInvoice.getStripeInvoiceId())) {
                try {
                    stripeInvoice = paymentService.getByInvoiceId(unpaidInvoice.getStripeInvoiceId());
                    if ("paid".equalsIgnoreCase(stripeInvoice.getStatus())) {
                        invoiceService.markAsPaid(unpaidInvoice.getId());
                        log.warn("Invoice [" + unpaidInvoice.getId() + "] already paid");
                        continue;

                    } else if ("void".equalsIgnoreCase(stripeInvoice.getStatus())) {
                        invoiceService.markAsVoid(unpaidInvoice.getId());
                        log.warn("Invoice [" + unpaidInvoice.getId() + "] already voided");
                        continue;
                    }
                } catch (GrabbillException e) {
                    // log and skip this invoice
                    log.warn(e.getMessage(), e);
                    continue;
                }
            }


            try {
                stripeInvoice = paymentService.chargeInvoice(unpaidInvoice.getStripeInvoiceId());
            } catch (GrabbillException e) {
                // log and skip this invoice
                log.warn(e.getMessage(), e);
                continue;
            }

            if ("paid".equalsIgnoreCase(stripeInvoice.getStatus())) {
                invoiceService.markAsPaid(unpaidInvoice.getId());
                log.warn("Invoice [" + unpaidInvoice.getId() + "] marked as paid");

            } else if ("open".equalsIgnoreCase(stripeInvoice.getStatus())) {
                invoiceService.markAsPaymentFailed(unpaidInvoice);
                log.warn("Invoice [" + unpaidInvoice.getId() + "] marked as payment failed");

            } else if ("void".equalsIgnoreCase(stripeInvoice.getStatus())) {
                invoiceService.markAsVoid(unpaidInvoice.getId());
                log.warn("Invoice [" + unpaidInvoice.getId() + "] marked as voided");
            }
        }
    }

    private Plan getMatchingPlan(final AccountSubscription accountSubscription, final List<Plan> allPlans) {
        for (Plan plan : allPlans) {

            boolean storageOptionMatched = false;
            for (StoragePlanOption storageOption : plan.getStorageOptions()) {
                if (storageOption.getSize().equals(accountSubscription.getStorageSize())
                        && storageOption.getPrice().equals(accountSubscription.getStoragePrice())) {
                    storageOptionMatched = true;
                    break;
                }
            }

            boolean transactionalEmailOptionMatched = false;
            for (TransactionalEmailPlanOption transactionalEmailOption : plan.getTransactionalEmailOptions()) {
                if (transactionalEmailOption.getSize().equals(accountSubscription.getTransactionalEmailSize())
                        && transactionalEmailOption.getPrice().equals(accountSubscription.getTransactionalEmailPrice())) {
                    transactionalEmailOptionMatched = true;
                    break;
                }
            }

            boolean emailCampaignOptionMatched = false;
            for (EmailCampaignPlanOption emailCampaignOption : plan.getEmailCampaignOptions()) {
                if (emailCampaignOption.getSize().equals(accountSubscription.getEmailCampaignSize())
                        && emailCampaignOption.getPrice().equals(accountSubscription.getEmailCampaignPrice())) {
                    emailCampaignOptionMatched = true;
                    break;
                }
            }

            if (storageOptionMatched && transactionalEmailOptionMatched && emailCampaignOptionMatched) {
                return plan;
            }
        }

        return null;
    }

    private User getAccountOwner(final Account account) {
        for (User user : account.getUsers()) {
            if (user.getRole().isOwner()) {
                return user;
            }
        }

        return null;
    }

    private PromoCode getPromoCode(final AccountSubscription accountSubscription) {
        if (StringUtils.hasLength(accountSubscription.getPromoCode())) {
            String code = accountSubscription.getPromoCode();
            PromoCode promoCode = promoCodeService.getByCode(code).orElse(null);

            if (promoCode != null) {
                if (DiscountOccurrence.CYCLE.equals(accountSubscription.getDiscountOccurrence())) {

                    OffsetDateTime validTill = null;
                    if (SubscriptionMode.MONTHLY.equals(accountSubscription.getMode())) {
                        validTill = accountSubscription.getStartDate().plusMonths(accountSubscription.getDiscountOccurrenceCount());
                    } else if (SubscriptionMode.ANNUALLY.equals(accountSubscription.getMode())) {
                        validTill = accountSubscription.getStartDate().plusYears(accountSubscription.getDiscountOccurrenceCount());
                    }

                    if (validTill != null && (accountSubscription.getCycleEndDate().isBefore(validTill) || accountSubscription.getCycleEndDate().isEqual(validTill))) {
                        return promoCode;
                    }

                } else if (DiscountOccurrence.FOREVER.equals(accountSubscription.getDiscountOccurrence())) {
                    return promoCode;
                }
            }
        }

        return null;
    }

}
