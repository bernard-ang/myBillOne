package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTTransactionalEmailFile;
import com.grabbill.core.entity.MTTransactionalEmailIndexRow;
import com.grabbill.core.entity.MTTransactionalEmailRecord;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class MTTransactionalEmailIndexRowPayload extends BaseIndexRowFullPayload {

    private String emailContent;
    private boolean emailStatusSkipUnsubscribed;
    private boolean emailStatusSkipBounced;
    private boolean emailStatusSoftBounce;
    private boolean emailStatusHardBounce;
    private boolean emailDsnReceivedConfirmation;
    private String emailDsnMessage;

    private OffsetDateTime emailStatusUserReadTimestamp;


    public static MTTransactionalEmailIndexRowPayload from(final MTTransactionalEmailIndexRow indexRow) {
        MTTransactionalEmailIndexRowPayload instance = new MTTransactionalEmailIndexRowPayload();
        MTTransactionalEmailFile file = indexRow.getMtTransactionalEmailFile();
        MTTransactionalEmailRecord record = indexRow.getMtTransactionalEmailRecord();

        populate(instance, indexRow);
        populate(instance, file);
        populate(instance, record);

        return instance;
    }

    private static void populate(
            final MTTransactionalEmailIndexRowPayload instance,
            final MTTransactionalEmailIndexRow indexRow
    ) {
        instance.setIndexRowId(indexRow.getId());
        instance.setActivityName(indexRow.getMtTransactionalEmailActivity().getName());
        instance.setActivityId(indexRow.getMtTransactionalEmailActivity().getId());
        instance.copyFrom(indexRow);
    }

    private static void populate(
            final MTTransactionalEmailIndexRowPayload instance,
            final MTTransactionalEmailFile file
    ) {
        if(file != null) {
            instance.copyFrom(file.getId(), file);
        }
    }

    private static void populate(
            final MTTransactionalEmailIndexRowPayload instance,
            final MTTransactionalEmailRecord record
    ) {
        instance.setRecordId(record.getId());
        instance.copyFrom(record);
    }

    void copyFrom(MTTransactionalEmailRecord record) {
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
