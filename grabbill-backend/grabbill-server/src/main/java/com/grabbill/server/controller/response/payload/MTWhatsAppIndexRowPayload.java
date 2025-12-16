package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTWhatsAppFile;
import com.grabbill.core.entity.MTWhatsAppIndexRow;
import com.grabbill.core.entity.MTWhatsAppRecord;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class MTWhatsAppIndexRowPayload extends BaseIndexRowFullPayload {

    private boolean whatsAppStatusSent;
    private boolean whatsAppStatusSkip;
    private boolean whatsAppStatusDelivered;
    private boolean whatsAppStatusRead;
    private boolean whatsAppStatusAcknowledge;


    public static MTWhatsAppIndexRowPayload from(final MTWhatsAppIndexRow indexRow) {
        MTWhatsAppIndexRowPayload instance = new MTWhatsAppIndexRowPayload();
        MTWhatsAppFile file = indexRow.getMtWhatsAppFile();
        MTWhatsAppRecord record = indexRow.getMtWhatsAppRecord();

        populate(instance, indexRow);
        populate(instance, file);
        populate(instance, record);

        return instance;
    }

    private static void populate(
            final MTWhatsAppIndexRowPayload instance,
            final MTWhatsAppIndexRow indexRow
    ) {
        instance.setIndexRowId(indexRow.getId());
        instance.setActivityName(indexRow.getMtWhatsAppActivity().getName());
        instance.setActivityId(indexRow.getMtWhatsAppActivity().getId());
        instance.copyFrom(indexRow);
    }

    private static void populate(
            final MTWhatsAppIndexRowPayload instance,
            final MTWhatsAppFile file
    ) {
        if(file != null) {
            instance.copyFrom(file.getId(), file);
        }
    }

    private static void populate(
            final MTWhatsAppIndexRowPayload instance,
            final MTWhatsAppRecord record
    ) {
        instance.setRecordId(record.getId());
        instance.copyFrom(record);
    }

    void copyFrom(MTWhatsAppRecord record) {
        super.copyFrom(record);
        this.setWhatsAppStatusSent(record.getWhatsAppStatusSent() != null && record.getWhatsAppStatusSent());
        this.setWhatsAppStatusSkip(record.getWhatsAppStatusSkip() != null && record.getWhatsAppStatusSkip());
        this.setWhatsAppStatusDelivered(record.getWhatsAppStatusDelivered() != null && record.getWhatsAppStatusDelivered());
        this.setWhatsAppStatusRead(record.getWhatsAppStatusRead() != null && record.getWhatsAppStatusRead());
        this.setWhatsAppStatusAcknowledge(record.getWhatsAppStatusAcknowledge() != null && record.getWhatsAppStatusAcknowledge());
    }

}
