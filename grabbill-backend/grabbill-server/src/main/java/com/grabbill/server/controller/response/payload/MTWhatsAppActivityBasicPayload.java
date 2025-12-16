package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTWhatsAppActivity;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class MTWhatsAppActivityBasicPayload extends BaseActivityBasicPayload {
    private int whatsAppTotal;
    private int whatsAppStatusSent;
    private int whatsAppStatusSkip;
    private int whatsAppStatusRead;
    private int whatsAppStatusDelivered;
    private int whatsAppStatusAcknowledge;
    private int whatsAppStatusFailed;

    public static MTWhatsAppActivityBasicPayload from(final MTWhatsAppActivity whatsAppActivity) {
        MTWhatsAppActivityBasicPayload instance = new MTWhatsAppActivityBasicPayload();
        instance.setId(whatsAppActivity.getId());
        instance.setNoOfFiles(whatsAppActivity.getMtWhatsAppFiles().size());
        instance.copyFrom(whatsAppActivity);

        instance.setWhatsAppTotal(whatsAppActivity.getMtWhatsAppIndexRows().size());
        instance.setWhatsAppStatusSent(whatsAppActivity.getWhatsAppStatusSent());
        instance.setWhatsAppStatusSkip(whatsAppActivity.getWhatsAppStatusSkip());
        instance.setWhatsAppStatusRead(whatsAppActivity.getWhatsAppStatusRead());
        instance.setWhatsAppStatusDelivered(whatsAppActivity.getWhatsAppStatusDelivered());
        instance.setWhatsAppStatusAcknowledge(whatsAppActivity.getWhatsAppStatusAcknowledge());
        instance.setWhatsAppStatusFailed(whatsAppActivity.getWhatsAppStatusFailed());

        return instance;
    }

}
