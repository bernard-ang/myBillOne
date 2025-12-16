package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.AccountStatement;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class AccountStatementPayload implements ApiPayload {

    private Integer id;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Long totalStorageUsed;
    private Long maxStorageSize;
    private Long totalTransactionalEmailSent;
    private Long maxTransactionalEmailSent;
    private Long totalEmailCampaignSent;
    private Long maxEmailCampaignSent;
    private Long totalWhatsappMessageSent;
    private Long totalSmsSent;
    private Integer smsCreditBalance;
    private Integer smsCreditUsed;
    private Double storagePrice;
    private Double transactionalEmailPrice;
    private Double emailCampaignPrice;
    private Double totalPrice;
    private OffsetDateTime createdDate;


    public static AccountStatementPayload from(
            final AccountStatement accountStatement
    ) {
        AccountStatementPayload payload = new AccountStatementPayload();

        payload.setId(accountStatement.getId());
        payload.setStartDate(accountStatement.getStartDate());
        payload.setEndDate(accountStatement.getEndDate());
        payload.setTotalStorageUsed(accountStatement.getTotalStorageUsed());
        payload.setMaxStorageSize(accountStatement.getMaxStorageSize());
        payload.setTotalTransactionalEmailSent(accountStatement.getTotalTransactionalEmailSent());
        payload.setMaxTransactionalEmailSent(accountStatement.getMaxTransactionalEmailSent());
        payload.setTotalEmailCampaignSent(accountStatement.getTotalEmailCampaignSent());
        payload.setMaxEmailCampaignSent(accountStatement.getMaxEmailCampaignSent());
        payload.setTotalWhatsappMessageSent(accountStatement.getTotalWhatsappMessageSent() == null ? 0: accountStatement.getTotalWhatsappMessageSent());
        payload.setTotalSmsSent(accountStatement.getTotalSmsSent());
        payload.setSmsCreditBalance(accountStatement.getSmsCreditBalance());
        payload.setSmsCreditUsed(accountStatement.getSmsCreditUsed());
        payload.setStoragePrice(accountStatement.getStoragePrice());
        payload.setTransactionalEmailPrice(accountStatement.getTransactionalEmailPrice());
        payload.setEmailCampaignPrice(accountStatement.getEmailCampaignPrice());
        payload.setTotalPrice(accountStatement.getTotalPrice());
        payload.setCreatedDate(accountStatement.getCreatedDate());

        return payload;
    }

}
