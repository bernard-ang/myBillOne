package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTWhatsAppRecord;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class MTWhatsAppRecordPayload extends BaseRecordPayload {

    OffsetDateTime processedTimestamp;
    private String whatsAppMessageId;
    private String whatsAppBodyContent;
    private boolean whatsAppStatusSent;
    private OffsetDateTime whatsAppStatusSentTimestamp;
    private boolean whatsAppStatusSkip;
    private String whatsAppStatusSkipReason;
    private boolean whatsAppStatusDelivered;
    private OffsetDateTime whatsAppStatusDeliveredTimestamp;
    private boolean whatsAppStatusRead;
    private OffsetDateTime whatsAppStatusReadTimestamp;
    private boolean whatsAppStatusAcknowledge;
    private OffsetDateTime whatsAppStatusAcknowledgeTimestamp;
    private boolean whatsAppStatusFailed;
    private OffsetDateTime whatsAppStatusFailedTimestamp;
    private String whatsAppStatusFailedMessage;

    void copyFrom(final MTWhatsAppRecord record) {
        super.copyFrom(record);
        this.setId(record.getId());
        this.setProcessedTimestamp(record.getProcessedTimestamp());
        this.setWhatsAppBodyContent(record.getWhatsAppBodyContent());

        this.setWhatsAppStatusSent(record.getWhatsAppStatusSent() != null && record.getWhatsAppStatusSent());
        this.setWhatsAppStatusSentTimestamp(record.getWhatsAppStatusSentTimestamp());

        this.setWhatsAppStatusSkip(record.getWhatsAppStatusSkip() != null && record.getWhatsAppStatusSkip());
        this.setWhatsAppStatusSkipReason(record.getWhatsAppStatusSkipReason());

        this.setWhatsAppStatusDelivered(record.getWhatsAppStatusDelivered() != null && record.getWhatsAppStatusDelivered());
        this.setWhatsAppStatusDeliveredTimestamp(record.getWhatsAppStatusDeliveredTimestamp());

        this.setWhatsAppStatusRead(record.getWhatsAppStatusRead() != null && record.getWhatsAppStatusRead());
        this.setWhatsAppStatusReadTimestamp(record.getWhatsAppStatusReadTimestamp());

        this.setWhatsAppStatusAcknowledge(record.getWhatsAppStatusAcknowledge() != null && record.getWhatsAppStatusAcknowledge());
        this.setWhatsAppStatusAcknowledgeTimestamp(record.getWhatsAppStatusAcknowledgeTimestamp());

        this.setWhatsAppStatusFailed(record.getWhatsAppStatusFailed() != null && record.getWhatsAppStatusFailed());
        this.setWhatsAppStatusFailedTimestamp(record.getWhatsAppStatusFailedTimestamp());
        this.setWhatsAppStatusFailedMessage(record.getWhatsAppStatusFailedMessage());
    }

}
