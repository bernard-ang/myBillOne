package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.service.AccountService;
import com.grabbill.core.service.payment.CustomerService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.StripeSessionType;
import com.grabbill.server.controller.request.UserPlanUpdateRequest;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.HostedPaymentUIPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/stripe/sessions")
public class StripeSessionController {

    private static final String MYR = "MYR";

    @Value("${payment.stripe.session.checkout.success.url}")
    private String successUrl;

    @Value("${payment.stripe.session.checkout.cancel.url}")
    private String cancelUrl;

    @Value("${payment.stripe.session.billing-portal.return.url}")
    private String returnUrl;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<GrabbillApiResponse> createSetupSession(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam StripeSessionType type,
            @RequestBody(required = false) UserPlanUpdateRequest request) {
        Account account = userDetails.getUser().getAccount();

        // Skip Stripe operations for payment-exempted accounts
        if (account.isPaymentExempted()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB0001,
                    "Payment operations are not available for exempted accounts");
        }

        if (!StringUtils.hasLength(account.getStripeCustomerId())) {

            try {
                Customer customer = customerService.getOrCreate(account);
                if (customer != null) {
                    account.setStripeCustomerId(customer.getId());
                    account = accountService.save(account);
                }

            } catch (GrabbillException e) {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB0001,
                        "Failed to create Stripe customer instance for account with id [" + account.getId() + "]!");
            }
        }

        String url;
        if (StripeSessionType.PAYMENT_SETUP_SESSION.equals(type)) {
            url = createPaymentSetupSession(account, request).getUrl();

        } else if (StripeSessionType.PAYMENT_UPDATE_SESSION.equals(type)) {
            url = createPaymentUpdateSession(account).getUrl();

        } else {
            throw new UnsupportedOperationException("Session type [" + type.name() + "] is not supported!");
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        HostedPaymentUIPayload.from(url)));
    }

    private com.stripe.model.checkout.Session createPaymentSetupSession(
            final Account account, final UserPlanUpdateRequest request) {
        com.stripe.param.checkout.SessionCreateParams params = com.stripe.param.checkout.SessionCreateParams.builder()
                .setMode(com.stripe.param.checkout.SessionCreateParams.Mode.SETUP)
                .addPaymentMethodType(com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.CARD)
                .setCustomer(account.getStripeCustomerId())
                .setClientReferenceId(account.getStripeCustomerId())
                .setCurrency(MYR)
                .setBillingAddressCollection(
                        com.stripe.param.checkout.SessionCreateParams.BillingAddressCollection.REQUIRED)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata("planId", request.getPlanId())
                .putMetadata("storageSize", request.getStorageSize().toString())
                .putMetadata("emailCampaignSize", request.getEmailCampaignSize().toString())
                .putMetadata("transactionalEmailSize", request.getTransactionalEmailSize().toString())
                .putMetadata("subscriptionMode", request.getSubscriptionMode().toString())
                .putMetadata("promoCode", request.getPromoCode())
                .build();

        try {
            return com.stripe.model.checkout.Session.create(params);
        } catch (StripeException e) {
            log.error("Failed to create payment setup session!", e);
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB0001,
                    "Failed to create payment setup session!");
        }
    }

    private com.stripe.model.billingportal.Session createPaymentUpdateSession(final Account account) {
        com.stripe.param.billingportal.SessionCreateParams params = com.stripe.param.billingportal.SessionCreateParams
                .builder()
                .setCustomer(account.getStripeCustomerId())
                .setReturnUrl(returnUrl)
                .build();

        try {
            return com.stripe.model.billingportal.Session.create(params);
        } catch (StripeException e) {
            log.error("Failed to create payment update session!", e);
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB0001,
                    "Failed to create payment update session!");
        }
    }

}
