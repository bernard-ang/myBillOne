package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Invoice;
import com.grabbill.core.entity.PromoCode;
import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.DiscountOccurrence;
import com.grabbill.core.model.DiscountType;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.model.InvoiceType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class InvoicePayload implements ApiPayload {

    private Integer id;
    private String invoiceNo;
    private String planName;
    private String planDescription;
    private Long storageSize;
    private Double storagePrice;
    private Long transactionalEmailSize;
    private Double transactionalEmailPrice;
    private Long emailCampaignSize;
    private Double emailCampaignPrice;
    private Long smsTopupSize;
    private Double smsTopupPrice;
    private OffsetDateTime cycleStartDate;
    private OffsetDateTime cycleEndDate;
    private Double totalAmount;
    private Double overdueAmount;
    private Double discountAmount;
    private Double totalTaxPercentage;
    private Double totalTaxAmount;
    private Double totalAmountWithTax;
    private Double offsetAmount;
    private String billToName;
    private String billToContactNo;
    private String billToEmail;
    private String billToAddrLine1;
    private String billToAddrLine2;
    private String billToCity;
    private String billToState;
    private String billToPostcode;
    private String billToCountry;
    private InvoiceStatus status;
    private InvoiceType type;

    private Integer accountId;
    private String accountName;

    private String createdBy;
    private OffsetDateTime createdDate;
    private String lastModifiedBy;
    private OffsetDateTime lastModifiedDate;

    private List<StripeEventPayload> events = new ArrayList<>();
    private PromoCode promoCode;
    private double totalAmountBeforeDiscount;

    @Data
    public static class PromoCode {
        private String name;
        private String code;
        private Integer discount;
        private DiscountType discountType;
        private DiscountOccurrence discountOccurrence;
        private Integer discountOccurrenceCount;
    }

    private AccountSubscriptionPayload accountSubscription;


    public static InvoicePayload from (
            final Invoice invoice,
            final List<StripeEvent> events,
            final com.grabbill.core.entity.PromoCode promoCode,
            final double totalAmountBeforeDiscount,
            final AccountSubscriptionPayload accountSubscription
    ) {
        InvoicePayload payload = new InvoicePayload();
        payload.setId(invoice.getId());
        payload.setInvoiceNo(invoice.getInvoiceNo());
        payload.setPlanName(invoice.getPlanName());
        payload.setPlanDescription(invoice.getPlanDescription());
        payload.setStorageSize(invoice.getStorageSize());
        payload.setEmailCampaignSize(invoice.getEmailCampaignSize());
        payload.setTransactionalEmailSize(invoice.getTransactionalEmailSize());
        payload.setStoragePrice(invoice.getStoragePrice());
        payload.setEmailCampaignPrice(invoice.getEmailCampaignPrice());
        payload.setTransactionalEmailPrice(invoice.getTransactionalEmailPrice());
        payload.setSmsTopupSize(invoice.getSmsTopupSize());
        payload.setSmsTopupPrice(invoice.getSmsTopupPrice());
        payload.setCycleStartDate(invoice.getCycleStartDate());
        payload.setCycleEndDate(invoice.getCycleEndDate());
        payload.setTotalAmount(invoice.getTotalAmount());
        payload.setOverdueAmount(invoice.getOverdueAmount());
        payload.setDiscountAmount(invoice.getDiscountAmount());
        payload.setTotalTaxPercentage(invoice.getTotalTaxPercentage());
        payload.setTotalTaxAmount(invoice.getTotalTaxAmount());
        payload.setTotalAmountWithTax(invoice.getTotalAmountWithTax());
        payload.setOffsetAmount(invoice.getOffsetAmount());
        payload.setBillToName(invoice.getBillToName());
        payload.setBillToContactNo(invoice.getBillToContactNo());
        payload.setBillToEmail(invoice.getBillToEmail());
        payload.setBillToAddrLine1(invoice.getBillToAddrLine1());
        payload.setBillToAddrLine2(invoice.getBillToAddrLine2());
        payload.setBillToCity(invoice.getBillToCity());
        payload.setBillToState(invoice.getBillToState());
        payload.setBillToPostcode(invoice.getBillToPostcode());
        payload.setBillToCountry(invoice.getBillToCountry());
        payload.setStatus(invoice.getStatus());
        payload.setType(invoice.getType());

        if (promoCode != null) {
            PromoCode promoCodePayload = new PromoCode();
            promoCodePayload.setName(promoCode.getName());
            promoCodePayload.setCode(promoCode.getCode());
            promoCodePayload.setDiscount(promoCode.getDiscount());
            promoCodePayload.setDiscountType(promoCode.getDiscountType());
            promoCodePayload.setDiscountOccurrence(promoCode.getDiscountOccurrence());
            promoCodePayload.setDiscountOccurrenceCount(promoCode.getDiscountOccurrenceCount());

            payload.setPromoCode(promoCodePayload);
        }

        payload.setTotalAmountBeforeDiscount(totalAmountBeforeDiscount);

        payload.setAccountId(invoice.getAccount().getId());
        payload.setAccountName(invoice.getAccount().getCompanyName());

        payload.setCreatedBy(invoice.getCreatedBy());
        payload.setCreatedDate(invoice.getCreatedDate());
        payload.setLastModifiedBy(invoice.getLastModifiedBy());
        payload.setLastModifiedDate(invoice.getLastModifiedDate());

        for (StripeEvent event : events) {
            payload.getEvents().add(StripeEventPayload.from(event));
        }

        payload.setAccountSubscription(accountSubscription);

        return payload;
    }
}
