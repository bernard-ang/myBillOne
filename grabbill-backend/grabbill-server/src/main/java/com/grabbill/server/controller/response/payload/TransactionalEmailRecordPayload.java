package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.TransactionalEmailRecord;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author seez
 */
@Data
public class TransactionalEmailRecordPayload extends BaseRecordPayload {

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

    private String whatsappMessageId;
    private String whatsappBodyContent;
    private boolean whatsappStatusSent;
    private OffsetDateTime whatsappStatusSentTimestamp;
    private boolean whatsappStatusSkip;
    private String whatsappStatusSkipReason;
    private boolean whatsappStatusDelivered;
    private OffsetDateTime whatsappStatusDeliveredTimestamp;
    private boolean whatsappStatusRead;
    private OffsetDateTime whatsappStatusReadTimestamp;
    private boolean whatsappStatusAcknowledge;
    private OffsetDateTime whatsappStatusAcknowledgeTimestamp;
    private boolean whatsappStatusFailed;
    private OffsetDateTime whatsappStatusFailedTimestamp;
    private String whatsappStatusFailedMessage;

    void copyFrom(final TransactionalEmailRecord record) {
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

        this.setWhatsappBodyContent(record.getWhatsappBodyContent());
        this.setWhatsappStatusSent(record.getWhatsappStatusSent() != null && record.getWhatsappStatusSent());
        this.setWhatsappStatusSentTimestamp(record.getWhatsappStatusSentTimestamp());
        this.setWhatsappStatusSkip(record.getWhatsappStatusSkip() != null && record.getWhatsappStatusSkip());
        this.setWhatsappStatusSkipReason(record.getWhatsappStatusSkipReason());
        this.setWhatsappStatusDelivered(record.getWhatsappStatusDelivered() != null && record.getWhatsappStatusDelivered());
        this.setWhatsappStatusDeliveredTimestamp(record.getWhatsappStatusDeliveredTimestamp());
        this.setWhatsappStatusRead(record.getWhatsappStatusRead() != null && record.getWhatsappStatusRead());
        this.setWhatsappStatusReadTimestamp(record.getWhatsappStatusReadTimestamp());
        this.setWhatsappStatusAcknowledge(record.getWhatsappStatusAcknowledge() != null && record.getWhatsappStatusAcknowledge());
        this.setWhatsappStatusAcknowledgeTimestamp(record.getWhatsappStatusAcknowledgeTimestamp());
        this.setWhatsappStatusFailed(record.getWhatsAppStatusFailed() != null && record.getWhatsAppStatusFailed());
        this.setWhatsappStatusFailedTimestamp(record.getWhatsAppStatusFailedTimestamp());
        this.setWhatsappStatusFailedMessage(record.getWhatsAppStatusFailedMessage());
    }

}
