package com.grabbill.core.service.payment;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.SubscriptionMode;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;

/**
 * @author michaellow
 */
public interface PaymentService {

    Invoice getByInvoiceId(String invoiceId);

    Invoice createInvoice(
            Account account,
            AccountSubscription newAccountSubscription,
            Integer planId,
            SubscriptionMode subscriptionMode,
            StoragePlanOption storageOption,
            TransactionalEmailPlanOption transactionalEmailOption,
            EmailCampaignPlanOption emailCampaignOption,
            double offsetAmount,
            PromoCode promoCode
    );

    Invoice createSmsCreditsTopupInvoice(Account account, CreditsPlanOption creditsPlanOption);

    Invoice chargeInvoice(String invoiceId);

    Invoice retryChargeInvoice(String invoiceId);

    PaymentIntent getPaymentIntentByInvoiceId(String invoiceId);

    PaymentMethod getPaymentMethodByInvoiceId(String invoiceId);

}
