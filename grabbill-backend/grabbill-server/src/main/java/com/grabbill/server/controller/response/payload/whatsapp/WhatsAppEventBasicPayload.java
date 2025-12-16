package com.grabbill.server.controller.response.payload.whatsapp;

import com.grabbill.core.entity.WhatsAppEvent;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class WhatsAppEventBasicPayload implements ApiPayload {

    private Long id;
    private String eventId;
    private String whatsappMessageId;
    private String type;
    private String messageType;
    private String status;
    private String content;
    private boolean processed;
    private Integer processedCount = 0;
    private OffsetDateTime eventTimestamp;
    private String mobileNo;


    public static WhatsAppEventBasicPayload from(final WhatsAppEvent whatsAppEvent) {
        WhatsAppEventBasicPayload payload = new WhatsAppEventBasicPayload();
        payload.setId(whatsAppEvent.getId());
        payload.setEventId(whatsAppEvent.getEventId());
        payload.setType(whatsAppEvent.getType());
        payload.setMessageType(whatsAppEvent.getMessageType());
        payload.setStatus(whatsAppEvent.getStatus());
        payload.setContent(whatsAppEvent.getContent());
        payload.setEventTimestamp(whatsAppEvent.getEventTimestamp());
        payload.setMobileNo(whatsAppEvent.getMobileNo());
        return payload;
    }

}
