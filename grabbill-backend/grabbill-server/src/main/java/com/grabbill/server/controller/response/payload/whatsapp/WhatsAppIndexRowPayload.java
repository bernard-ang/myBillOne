package com.grabbill.server.controller.response.payload.whatsapp;

import com.grabbill.core.entity.*;
import com.grabbill.server.controller.response.payload.BaseIndexRowFullPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author seez
 */
@Data
public class WhatsAppIndexRowPayload extends BaseIndexRowFullPayload {

    private boolean whatsAppStatusSent;
    private boolean whatsAppStatusSkip;
    private boolean whatsAppStatusDelivered;
    private boolean whatsAppStatusRead;
    private boolean whatsAppStatusAcknowledge;


    public static WhatsAppIndexRowPayload from(final WhatsAppIndexRow indexRow) {
        WhatsAppIndexRowPayload instance = new WhatsAppIndexRowPayload();
        WhatsAppFile file = indexRow.getWhatsAppFile();
        WhatsAppRecord record = indexRow.getWhatsAppRecord();

        populate(instance, indexRow);
        populate(instance, file);
        populate(instance, record);

        return instance;
    }

    private static void populate(
            final WhatsAppIndexRowPayload instance,
            final WhatsAppIndexRow indexRow
    ) {
        instance.setIndexRowId(indexRow.getId());
        instance.setActivityName(indexRow.getWhatsAppActivity().getName());
        instance.setActivityId(indexRow.getWhatsAppActivity().getId());
        instance.copyFrom(indexRow);
    }

    private static void populate(
            final WhatsAppIndexRowPayload instance,
            final WhatsAppFile file
    ) {
        if(file != null) {
            instance.copyFrom(file.getId(), file);
        }
    }

    private static void populate(
            final WhatsAppIndexRowPayload instance,
            final WhatsAppRecord record
    ) {
        instance.setRecordId(record.getId());
        instance.copyFrom(record);
    }

    void copyFrom(WhatsAppRecord record) {
        super.copyFrom(record);
        this.setWhatsAppStatusSent(record.getWhatsAppStatusSent() != null && record.getWhatsAppStatusSent());
        this.setWhatsAppStatusSkip(record.getWhatsAppStatusSkip() != null && record.getWhatsAppStatusSkip());
        this.setWhatsAppStatusDelivered(record.getWhatsAppStatusDelivered() != null && record.getWhatsAppStatusDelivered());
        this.setWhatsAppStatusRead(record.getWhatsAppStatusRead() != null && record.getWhatsAppStatusRead());
        this.setWhatsAppStatusAcknowledge(record.getWhatsAppStatusAcknowledge() != null && record.getWhatsAppStatusAcknowledge());
    }
}
