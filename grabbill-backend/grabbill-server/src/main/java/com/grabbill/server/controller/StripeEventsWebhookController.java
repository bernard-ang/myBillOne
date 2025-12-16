package com.grabbill.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.StripeEventType;
import com.grabbill.core.service.AccountService;
import com.grabbill.core.service.payment.StripeEventService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.model.billingportal.Session;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/webhook/stripe/events")
public class StripeEventsWebhookController {

    @Value("${payment.stripe.endpoint.secret}")
    private String stripeEndpointSecret;

    @Autowired
    private AccountService accountService;

    @Autowired
    private StripeEventService stripeEventService;

    @Autowired
    private ObjectMapper objectMapper;


    @PostMapping
    public ResponseEntity<String> handleEvent(
            @RequestHeader MultiValueMap<String, String> headers,
            @RequestBody String jsonPayload
    ) {
        try {
            Event event = verifyPayload(jsonPayload, getSignature(headers));
            log.debug(event.getType() + " received:\n" + jsonPayload);

            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            if (dataObjectDeserializer.getObject().isPresent()) {
                processInternal(event, dataObjectDeserializer.getObject().get(), event.toJson());
            } else {
                log.warn("Unabled to deserialize stripe object from event:\n" + jsonPayload);
            }

        } catch (SignatureVerificationException | JsonProcessingException e) {
            log.error("Failed to process stripe event.", e);
            log.error("Raw json payload:" + jsonPayload);
            return ResponseEntity.badRequest().body(jsonPayload);
        }

        return ResponseEntity.ok("");
    }

    private void processInternal(
            final Event event,
            final StripeObject stripeObject,
            final String objectString
    ) {
        switch (event.getType()) {

            case "setup_intent.canceled":
            case "setup_intent.created":
            case "setup_intent.succeeded":
            case "setup_intent.setup_failed":
            case "setup_intent.requires_action":
                handleSetupIntentEvent(event, (SetupIntent) stripeObject, objectString);
                break;

            case "billing_portal.session.created":
                handleBillingPortalSessionEvent(event, (Session) stripeObject, objectString);
                break;

            case "payment_method.attached":
            case "payment_method.detached":
            case "payment_method.updated":
                handlePaymentMethodEvent(event, (PaymentMethod) stripeObject, objectString);
                break;

            case "invoice.created":
            case "invoice.deleted":
            case "invoice.finalized":
            case "invoice.finalization_failed":
            case "invoice.paid":
            case "invoice.marked_uncollectible":
            case "invoice.payment_action_required":
            case "invoice.payment_failed":
            case "invoice.payment_succeeded":
            case "invoice.updated":
            case "invoice.voided":
                handleInvoiceEvent(event, (Invoice) stripeObject, objectString);
                break;

            default:
                log.info("Unhandled event type - " + event.getType());
        }
    }

    private void handleSetupIntentEvent(
            final Event event,
            final SetupIntent setupIntent,
            final String objectString
    ) {
        String customerId = setupIntent.getCustomer();
        com.grabbill.core.entity.Account account = getAccount(customerId);
        logStripeEvent(event, StripeEventType.SETUP_INTENT, customerId, objectString, account);
    }

    private void handleBillingPortalSessionEvent(
            final Event event,
            final Session billingPortalSession,
            final String objectString
    ) {
        String customerId = billingPortalSession.getCustomer();
        com.grabbill.core.entity.Account account = getAccount(customerId);
        logStripeEvent(event, StripeEventType.BILLING_PORTAL, customerId, objectString, account);
    }


    private void handlePaymentMethodEvent(
            final Event event,
            final PaymentMethod paymentMethod,
            final String objectString
    ) {
        String customerId = paymentMethod.getCustomer();
        com.grabbill.core.entity.Account account = getAccount(customerId);
        logStripeEvent(event, StripeEventType.PAYMENT_METHOD, customerId, objectString, account);
    }

    private void handleInvoiceEvent(
            final Event event,
            final Invoice invoice,
            final String objectString
    ) {
        String customerId = invoice.getCustomer();
        com.grabbill.core.entity.Account account = getAccount(customerId);
        logStripeEvent(event, StripeEventType.INVOICE, invoice.getId(), objectString, account);
    }

    private void logStripeEvent(
            final Event event,
            final StripeEventType type,
            final String refId,
            final String objectString,
            final com.grabbill.core.entity.Account account
            ) {
        StripeEvent stripeEvent = new StripeEvent();
        stripeEvent.setEventId(event.getId());
        stripeEvent.setEventType(event.getType());
        stripeEvent.setType(type);
        stripeEvent.setRefId(refId);
        stripeEvent.setJsonObject(objectString);
        stripeEvent.setAccount(account);

        stripeEventService.save(stripeEvent);
    }

    private com.grabbill.core.entity.Account getAccount(final String stripeCustomerId) {
        com.grabbill.core.entity.Account account = null;
        if (StringUtils.hasLength(stripeCustomerId)) {
            account = accountService.getByStripeCustomerId(stripeCustomerId).orElse(null);
        }

        return account;
    }

    private Event verifyPayload(final String jsonPayload, final String signature)
            throws SignatureVerificationException, JsonProcessingException {

        if (StringUtils.hasLength(signature)) {
            return Webhook.constructEvent(jsonPayload, signature, stripeEndpointSecret);
        }

        return objectMapper.readValue(jsonPayload, Event.class);
    }

    private String getSignature(final MultiValueMap<String, String> headers) {
        String signatureHeader = null;
        if (!headers.get("stripe-signature").isEmpty()) {
            signatureHeader = headers.get("stripe-signature").get(0);

        } else if (!headers.get("Stripe-Signature").isEmpty()) {
            signatureHeader = headers.get("Stripe-Signature").get(0);
        }

        return signatureHeader;
    }

}
