package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.Invoice;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.model.InvoiceType;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class InvoiceBasicPayload implements ApiPayload {

    private Integer id;
    private String invoiceNo;
    private String planName;
    private Long storageSize;
    private Long transactionalEmailSize;
    private Long emailCampaignSize;
    private Long smsTopupSize;
    private Double smsTopupPrice;
    private OffsetDateTime cycleStartDate;
    private OffsetDateTime cycleEndDate;
    private Double totalAmountWithTax;
    private String billToName;
    private String billToContactNo;
    private String billToEmail;
    private InvoiceStatus status;
    private InvoiceType type;

    private Integer accountId;
    private String accountName;

    private String createdBy;
    private OffsetDateTime createdDate;
    private String lastModifiedBy;
    private OffsetDateTime lastModifiedDate;


    public static InvoiceBasicPayload from (final Invoice invoice) {
        InvoiceBasicPayload payload = new InvoiceBasicPayload();
        payload.setId(invoice.getId());
        payload.setInvoiceNo(invoice.getInvoiceNo());
        payload.setPlanName(invoice.getPlanName());
        payload.setStorageSize(invoice.getStorageSize());
        payload.setEmailCampaignSize(invoice.getEmailCampaignSize());
        payload.setTransactionalEmailSize(invoice.getTransactionalEmailSize());
        payload.setSmsTopupSize(invoice.getSmsTopupSize());
        payload.setSmsTopupPrice(invoice.getSmsTopupPrice());
        payload.setCycleStartDate(invoice.getCycleStartDate());
        payload.setCycleEndDate(invoice.getCycleEndDate());
        payload.setTotalAmountWithTax(invoice.getTotalAmountWithTax());
        payload.setBillToName(invoice.getBillToName());
        payload.setBillToContactNo(invoice.getBillToContactNo());
        payload.setBillToEmail(invoice.getBillToEmail());
        payload.setStatus(invoice.getStatus());
        payload.setType(invoice.getType());

        payload.setAccountId(invoice.getAccount().getId());
        payload.setAccountName(invoice.getAccount().getCompanyName());

        payload.setCreatedBy(invoice.getCreatedBy());
        payload.setCreatedDate(invoice.getCreatedDate());
        payload.setLastModifiedBy(invoice.getLastModifiedBy());
        payload.setLastModifiedDate(invoice.getLastModifiedDate());

        return payload;
    }
}
