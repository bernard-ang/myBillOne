package com.grabbill.server.controller.request;

/**
 * @author michaellow
 */
public enum StripeSessionType {

    // checkout session of SetupIntent type (Stripe's hosted checkout UI for bind card)
    PAYMENT_SETUP_SESSION,

    // billing portal session (Stripe's hosted customer portal UI for payment method / billing info management)
    PAYMENT_UPDATE_SESSION

}
