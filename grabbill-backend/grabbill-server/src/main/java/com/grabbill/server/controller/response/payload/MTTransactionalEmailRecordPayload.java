package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTTransactionalEmailRecord;
import com.grabbill.core.entity.TransactionalEmailRecord;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author seez
 */
@Data
public class MTTransactionalEmailRecordPayload extends BaseRecordPayload {

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

    void copyFrom(final MTTransactionalEmailRecord record) {
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
