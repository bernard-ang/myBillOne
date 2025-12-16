package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.CreditType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.*;
import com.grabbill.core.service.payment.CustomerPaymentMethodService;
import com.grabbill.core.service.payment.CustomerService;
import com.grabbill.core.service.payment.PaymentService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.SmsCreditTopupRequest;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.SmsCreditsPlanOptionsPayload;
import com.grabbill.server.controller.response.payload.SmsRemainingCreditPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.PaymentNotificationEmailService;
import com.stripe.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/sms-credits")
public class SmsCreditController {

    @Value("${sms.endpoints.enabled:false}")
    private boolean enabled;

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;

    @Autowired
    private AccountUsageStatisticService accountUsageStatisticService;

    @Autowired
    private CreditsPlanOptionService creditsPlanOptionService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerPaymentMethodService customerPaymentMethodService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private PaymentNotificationEmailService paymentNotificationEmailService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AuditLogService auditLogService;


    @GetMapping(value = "/plan-options")
    public ResponseEntity<GrabbillApiResponse> getCreditsPlanOptions() {
        List<CreditsPlanOption> creditsPlanOptions = creditsPlanOptionService.getByType(CreditType.SMS);
        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        SmsCreditsPlanOptionsPayload.from(creditsPlanOptions)
                )
        );
    }

    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getRemainingCredits(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        Account account = user.getAccount();
        Optional<AccountUsageStatistic> accountUsageStatisticOptional =
                accountUsageStatisticService.getByAccountId(account.getId());
        if (!accountUsageStatisticOptional.isPresent()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB1036, "No usage statistic found for account [" + account.getId() + "].");
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        SmsRemainingCreditPayload.from(accountUsageStatisticOptional.get())
                )
        );
    }

    @Transactional
    @PostMapping(value = "/topup")
    public ResponseEntity<GrabbillApiResponse> topupSmsCredits(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody SmsCreditTopupRequest request
    ) {
        if (!enabled) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB9011, "Sms feature is disabled");
        }

        User user = userDetails.getUser();
        Account account = user.getAccount();
        AccountSubscription currentAccountSubscription = accountSubscriptionService.getActiveSubscriptionByAccountId(account.getId()).orElse(null);
        Optional<AccountUsageStatistic> accountUsageStatisticOptional =
                accountUsageStatisticService.getByAccountId(account.getId());
        if (accountUsageStatisticOptional.isEmpty()) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB1036, "No usage statistic found for account [" + account.getId() + "].");
        }
        AccountUsageStatistic accountUsageStatistic = accountUsageStatisticOptional.get();

        Customer customer = customerService.getOrCreate(account);
        com.stripe.model.PaymentMethod defaultPaymentMethod = customerPaymentMethodService.getDefaultPaymentMethodByCustomerId(customer.getId());
        if (defaultPaymentMethod == null) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB8003, "No registered payment method for account [" + account.getId() + "].");
        }

        // charge credit card
        BillingInformation billingInformation = customerService.getBillingInformation(customer, defaultPaymentMethod);
        CreditsPlanOption creditsPlanOption = creditsPlanOptionService.getById(request.getOptionId()).get();
        com.stripe.model.Invoice stripeInvoice = paymentService.createSmsCreditsTopupInvoice(account, creditsPlanOption);
        stripeInvoice = paymentService.chargeInvoice(stripeInvoice.getId());

        if (!"paid".equalsIgnoreCase(stripeInvoice.getStatus())) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB8006, "Failed to charge payment for sms credits topup for account [" + account.getId() + "].");
        }

        // topup credits
        int smsCredits = (accountUsageStatistic.getSmsCredit() != null) ?
                accountUsageStatistic.getSmsCredit() + creditsPlanOption.getQuantity() : creditsPlanOption.getQuantity();
        accountUsageStatistic.setSmsCredit(smsCredits);
        accountUsageStatistic = accountUsageStatisticService.save(accountUsageStatistic);

        // issue invoice
        Invoice invoice = invoiceService.createSmsTopupInvoice(
                user, stripeInvoice, creditsPlanOption, billingInformation, account, currentAccountSubscription);
        paymentNotificationEmailService.sendCreditsTopupEmail(invoice, stripeInvoice);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(account.getId()),
                DomainType.SMS,
                ActionType.CREDIT_TOPUP,
                "SMS credit topup",
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        SmsRemainingCreditPayload.from(accountUsageStatistic)
                )
        );
    }

}
