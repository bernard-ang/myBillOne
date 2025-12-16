package com.grabbill.engine.service;

import com.grabbill.core.entity.Invoice;

/**
 * @auchor michaellow
 */
public interface SubscriptionPaymentNotificationEmailService {

    String NAME = "name";
    String PLAN = "plan";
    String TOTAL_AMOUNT_WITH_TAX = "totalAmountWithTax";
    String INVOICE_NO = "invoiceNo";
    String START_DATE = "startDate";
    String END_DATE = "endDate";


    void sendEmail(Invoice invoice, com.stripe.model.Invoice stripeInvoice);

}
