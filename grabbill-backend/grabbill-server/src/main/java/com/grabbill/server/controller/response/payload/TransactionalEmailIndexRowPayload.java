package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.TransactionalEmailFile;
import com.grabbill.core.entity.TransactionalEmailIndexRow;
import com.grabbill.core.entity.TransactionalEmailRecord;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class TransactionalEmailIndexRowPayload extends BaseIndexRowFullPayload {

    private String emailContent;
    private boolean emailStatusSkipUnsubscribed;
    private boolean emailStatusSkipBounced;
    private boolean emailStatusSoftBounce;
    private boolean emailStatusHardBounce;
    private boolean emailDsnReceivedConfirmation;
    private String emailDsnMessage;

    private OffsetDateTime emailStatusUserReadTimestamp;


    public static TransactionalEmailIndexRowPayload from(final TransactionalEmailIndexRow indexRow) {
        TransactionalEmailIndexRowPayload instance = new TransactionalEmailIndexRowPayload();
        TransactionalEmailFile file = indexRow.getTransactionalEmailFile();
        TransactionalEmailRecord record = indexRow.getTransactionalEmailRecord();

        populate(instance, indexRow);
        populate(instance, file);
        populate(instance, record);

        return instance;
    }

    private static void populate(
            final TransactionalEmailIndexRowPayload instance,
            final TransactionalEmailIndexRow indexRow
    ) {
        instance.setIndexRowId(indexRow.getId());
        instance.setActivityName(indexRow.getTransactionalEmailActivity().getName());
        instance.setActivityId(indexRow.getTransactionalEmailActivity().getId());
        instance.copyFrom(indexRow);
    }

    private static void populate(
            final TransactionalEmailIndexRowPayload instance,
            final TransactionalEmailFile file
    ) {
        if(file != null) {
            instance.copyFrom(file.getId(), file);
        }
    }

    private static void populate(
            final TransactionalEmailIndexRowPayload instance,
            final TransactionalEmailRecord record
    ) {
        instance.setRecordId(record.getId());
        instance.copyFrom(record);
    }

    void copyFrom(TransactionalEmailRecord record) {
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
