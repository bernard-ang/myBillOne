package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.SmsRecord;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class SmsRecordPayload extends BaseRecordPayload {

    String smsContent;
    boolean smsStatusSent;
    int creditUsed;
    int smsStatusCode;
    String smsErrorMessage;
    OffsetDateTime processedTimestamp;


    void copyFrom(final SmsRecord record) {
        super.copyFrom(record);
        this.setSmsContent(record.getSmsContent());
        this.setId(record.getId());
        this.setSmsStatusSent(record.isSmsStatusSent());
        this.setCreditUsed(record.getCreditUsed());
        this.setSmsStatusCode(record.getSmsStatusCode());
        this.setSmsErrorMessage(record.getSmsErrorMessage());
        if (!record.isSmsStatusSent() && record.getSmsErrorMessage() == null) {
            this.setSmsErrorMessage(record.getMessage());
        }
        this.setProcessedTimestamp(record.getProcessedTimestamp());
    }

}
