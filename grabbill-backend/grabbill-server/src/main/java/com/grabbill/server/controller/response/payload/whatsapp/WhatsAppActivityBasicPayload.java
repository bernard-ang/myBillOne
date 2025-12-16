package com.grabbill.server.controller.response.payload.whatsapp;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.server.controller.response.payload.BaseActivityBasicPayload;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class WhatsAppActivityBasicPayload extends BaseActivityBasicPayload {
    private int whatsAppTotal;
    private int whatsAppStatusSent;
    private int whatsAppStatusSkip;
    private int whatsAppStatusRead;
    private int whatsAppStatusDelivered;
    private int whatsAppStatusAcknowledge;
    private int whatsAppStatusFailed;

    public static WhatsAppActivityBasicPayload from(
            final WhatsAppActivity whatsAppActivity
    ) {
        WhatsAppActivityBasicPayload instance = new WhatsAppActivityBasicPayload();
        instance.setId(whatsAppActivity.getId());
        instance.setNoOfFiles(whatsAppActivity.getWhatsAppFiles().size());
        instance.copyFrom(whatsAppActivity);

        instance.setWhatsAppTotal(whatsAppActivity.getWhatsAppIndexRows().size());
        instance.setWhatsAppStatusSent(whatsAppActivity.getWhatsAppStatusSent());
        instance.setWhatsAppStatusSkip(whatsAppActivity.getWhatsAppStatusSkip());
        instance.setWhatsAppStatusRead(whatsAppActivity.getWhatsAppStatusRead());
        instance.setWhatsAppStatusDelivered(whatsAppActivity.getWhatsAppStatusDelivered());
        instance.setWhatsAppStatusAcknowledge(whatsAppActivity.getWhatsAppStatusAcknowledge());
        instance.setWhatsAppStatusFailed(whatsAppActivity.getWhatsAppStatusFailed());

        return instance;
    }

}
