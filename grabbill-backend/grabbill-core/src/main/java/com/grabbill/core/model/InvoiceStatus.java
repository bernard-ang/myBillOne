package com.grabbill.core.model;

/**
 * @author michaellow
 */
public enum InvoiceStatus {

    // new / unpaid
    NEW,

    // payment processing (interim state)
    PROCESSING,

    // payment completed
    PAID,

    // payment failed
    PAYMENT_FAILED,

    // void by admin
    VOID,

    // payment waived (pilot account)
    PAYMENT_WAIVED,

    // rolled forward to charge in new invoice
    @Deprecated
    CARRY_FORWARD,

    // FOC plan
    @Deprecated
    FREE

}
