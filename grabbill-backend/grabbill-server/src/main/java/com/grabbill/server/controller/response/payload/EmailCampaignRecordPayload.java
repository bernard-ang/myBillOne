package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.EmailCampaignRecord;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class EmailCampaignRecordPayload extends BaseRecordPayload {

    String emailContent;
    boolean emailStatusSent;
    boolean emailStatusSkipUnsubscribed;
    boolean emailStatusSkipBounced;
    OffsetDateTime processedTimestamp;
    boolean emailStatusSoftBounce;
    boolean emailStatusHardBounce;
    boolean emailDsnReceivedConfirmation;
    String emailDsnMessage;
    OffsetDateTime dsnProcessedTimestamp;
    OffsetDateTime emailStatusUserReadTimestamp;
    private OffsetDateTime emailStatusUnsubscribedTimestamp;
    private String emailStatusUnsubscribedReason;
    BaseIndexRowPayload indexRow;


    void copyFrom(final EmailCampaignRecord record) {
        super.copyFrom(record);
        this.setEmailContent(record.getEmailContent());
        this.setId(record.getId());
        this.setEmailStatusSent(record.isEmailStatusSent());
        this.setEmailStatusSkipUnsubscribed(record.isEmailStatusSkipUnsubscribed());
        this.setEmailStatusSkipBounced(record.isEmailStatusSkipBounced());
        this.setProcessedTimestamp(record.getProcessedTimestamp());
        this.setEmailStatusSoftBounce(record.isEmailStatusSoftBounce());
        this.setEmailStatusHardBounce(record.isEmailStatusHardBounce());
        this.setEmailDsnReceivedConfirmation(record.isEmailDsnReceivedConfirmation());
        this.setEmailDsnMessage(record.getEmailDsnMessage());
        this.setDsnProcessedTimestamp(record.getDsnProcessedTimestamp());
        this.setEmailStatusUserReadTimestamp(record.getEmailStatusUserReadTimestamp());
        this.setEmailStatusUnsubscribedTimestamp(record.getEmailStatusUnsubscribedTimestamp());
        this.setEmailStatusUnsubscribedReason(record.getEmailStatusUnsubscribedReason());
    }

}
