package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class EmailCampaignIndexRowPayload extends BaseIndexRowFullPayload {

    private String emailContent;
    private boolean emailStatusSkipUnsubscribed;
    private boolean emailStatusSkipBounced;
    private boolean emailStatusSoftBounce;
    private boolean emailStatusHardBounce;
    private boolean emailDsnReceivedConfirmation;
    private String emailDsnMessage;

    private OffsetDateTime emailStatusUserReadTimestamp;


    public static EmailCampaignIndexRowPayload from(final EmailCampaignIndexRow indexRow) {
        EmailCampaignIndexRowPayload instance = new EmailCampaignIndexRowPayload();
        EmailCampaignRecord record = indexRow.getEmailCampaignRecord();

        populate(instance, indexRow);
        populate(instance, record);

        return instance;
    }

    private static void populate(
            final EmailCampaignIndexRowPayload instance,
            final EmailCampaignIndexRow indexRow
    ) {
        instance.setIndexRowId(indexRow.getId());
        instance.setActivityName(indexRow.getEmailCampaignActivity().getName());
        instance.setActivityId(indexRow.getEmailCampaignActivity().getId());
        instance.copyFrom(indexRow);
    }

    private static void populate(
            final EmailCampaignIndexRowPayload instance,
            final EmailCampaignRecord record
    ) {
        instance.setRecordId(record.getId());
        instance.copyFrom(record);
    }

    void copyFrom(EmailCampaignRecord record) {
        super.copyFrom(record);
        this.setEmailContent(record.getEmailContent());
        this.setEmailStatusSkipUnsubscribed(record.isEmailStatusSkipUnsubscribed());
        this.setEmailStatusSkipBounced(record.isEmailStatusSkipBounced());
        this.setEmailStatusSoftBounce(record.isEmailStatusSoftBounce());
        this.setEmailStatusHardBounce(record.isEmailStatusHardBounce());
        this.setEmailStatusUserReadTimestamp(record.getEmailStatusUserReadTimestamp());
        this.setEmailDsnReceivedConfirmation(record.isEmailDsnReceivedConfirmation());
        this.setEmailDsnMessage(record.getEmailDsnMessage());
    }
}
