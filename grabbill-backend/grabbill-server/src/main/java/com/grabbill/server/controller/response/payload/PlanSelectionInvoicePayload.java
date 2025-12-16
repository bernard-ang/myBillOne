package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.Invoice;
import com.grabbill.core.entity.PromoCode;
import com.grabbill.core.model.DiscountOccurrence;
import com.grabbill.core.model.DiscountType;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class PlanSelectionInvoicePayload implements ApiPayload {

    private String planName;
    private String planDescription;
    private Long storageSize;
    private Double storagePrice;
    private Long transactionalEmailSize;
    private Double transactionalEmailPrice;
    private Long emailCampaignSize;
    private Double emailCampaignPrice;
    private OffsetDateTime cycleStartDate;
    private OffsetDateTime cycleEndDate;
    private Double totalAmount;
    private Double totalTaxPercentage;
    private Double totalTaxAmount;
    private Double totalAmountWithTax;
    private SubscriptionMode mode;
    private InvoiceStatus status;

    private Double offsetAmount;
    private Double oldAmount;
    private Long remainingDays;
    private String prevPlanName;
    private String prevPlanDescription;
    private OffsetDateTime prevCycleStartDate;
    private OffsetDateTime prevCycleEndDate;

    private boolean paymentMethodAvailable;
    private boolean billingAddressAvailable;

    private PromoCode promoCode;
    private Double totalAmountBeforeDiscount;
    private Double discountAmount;

    @Data
    public static class PromoCode {
        private String name;
        private String code;
        private Integer discount;
        private DiscountType discountType;
        private DiscountOccurrence discountOccurrence;
        private Integer discountOccurrenceCount;
    }


    public static PlanSelectionInvoicePayload from (
            final Invoice invoice,
            final AccountSubscription currentSubscription,
            final double oldAmount,
            final double newAmountChargeable,
            final long remainingDays,
            final boolean paymentMethodAvailable,
            final boolean billingAddressAvailable,
            final com.grabbill.core.entity.PromoCode promoCode
    ) {
        PlanSelectionInvoicePayload payload = new PlanSelectionInvoicePayload();
        payload.setPlanName(invoice.getPlanName());
        payload.setPlanDescription(invoice.getPlanDescription());
        payload.setStorageSize(invoice.getStorageSize());
        payload.setEmailCampaignSize(invoice.getEmailCampaignSize());
        payload.setTransactionalEmailSize(invoice.getTransactionalEmailSize());
        payload.setStoragePrice(invoice.getStoragePrice());
        payload.setEmailCampaignPrice(invoice.getEmailCampaignPrice());
        payload.setTransactionalEmailPrice(invoice.getTransactionalEmailPrice());
        payload.setCycleStartDate(invoice.getCycleStartDate());
        payload.setCycleEndDate(invoice.getCycleEndDate());
        payload.setTotalAmount(invoice.getTotalAmount());
        payload.setTotalTaxPercentage(invoice.getTotalTaxPercentage());
        payload.setTotalTaxAmount(invoice.getTotalTaxAmount());
        payload.setTotalAmountWithTax(invoice.getTotalAmountWithTax());
        payload.setOffsetAmount(invoice.getOffsetAmount());
        payload.setMode(invoice.getAccountSubscription().getMode());
        payload.setStatus(invoice.getStatus());

        if (currentSubscription != null) {
            payload.setPrevPlanName(currentSubscription.getPlanName());
            payload.setPrevPlanDescription(currentSubscription.getPlanDescription());
            payload.setPrevCycleStartDate(currentSubscription.getCycleStartDate());
            payload.setPrevCycleEndDate(currentSubscription.getCycleEndDate());
        }
        payload.setOldAmount(oldAmount);
        payload.setRemainingDays(remainingDays);
        payload.setPaymentMethodAvailable(paymentMethodAvailable);
        payload.setBillingAddressAvailable(billingAddressAvailable);

        if (promoCode != null) {
            PromoCode promoCodeInstance = new PromoCode();
            promoCodeInstance.setName(promoCode.getName());
            promoCodeInstance.setCode(promoCode.getCode());
            promoCodeInstance.setDiscount(promoCode.getDiscount());
            promoCodeInstance.setDiscountType(promoCode.getDiscountType());
            promoCodeInstance.setDiscountOccurrence(promoCode.getDiscountOccurrence());
            promoCodeInstance.setDiscountOccurrenceCount(promoCode.getDiscountOccurrenceCount());

            payload.setPromoCode(promoCodeInstance);
        }
        payload.setTotalAmountBeforeDiscount(newAmountChargeable);
        payload.setDiscountAmount(invoice.getDiscountAmount());


        return payload;
    }
}
