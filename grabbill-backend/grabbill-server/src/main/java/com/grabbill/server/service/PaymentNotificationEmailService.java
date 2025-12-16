package com.grabbill.server.service;

import com.grabbill.core.entity.Invoice;
import com.grabbill.core.entity.PromoCode;

/**
 * @auchor michaellow
 */
public interface PaymentNotificationEmailService {

    String NAME = "name";
    String PLAN = "plan";
    String TOTAL_AMOUNT_WITH_TAX = "totalAmountWithTax";
    String INVOICE_NO = "invoiceNo";
    String START_DATE = "startDate";
    String END_DATE = "endDate";


    void sendEmail(Invoice invoice, com.stripe.model.Invoice stripeInvoice, PromoCode promoCode);

    void sendCreditsTopupEmail(Invoice invoice, com.stripe.model.Invoice stripeInvoice);

}
