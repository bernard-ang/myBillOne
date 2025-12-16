package com.grabbill.server.controller.response.payload.whatsapp;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailType;
import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppType;
import com.grabbill.server.controller.response.payload.BaseTypeBasicPayload;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author seez
 */
@Data
public class WhatsAppTypeBasicPayload extends BaseTypeBasicPayload {

    private String lastSentBy;

    private OffsetDateTime lastSentDate;

    private boolean hasAttachment;

    private boolean passwordProtected;

    public static WhatsAppTypeBasicPayload from (
            final WhatsAppType type,
            final List<WhatsAppActivity> activities
    ) {
        WhatsAppTypeBasicPayload instance = new WhatsAppTypeBasicPayload();
        instance.setId(type.getId());
        instance.setLastSentBy(type.getLastSentBy());
        instance.setLastSentDate(type.getLastSentDate());
        instance.setHasAttachment(type.isHasAttachment());
        instance.setPasswordProtected(type.isPasswordProtected());
        instance.copyFrom(type);

        int noOfFiles = 0;
        for (WhatsAppActivity activity : activities) {
            noOfFiles += activity.getWhatsAppFiles().size();
        }
        instance.setNoOfFiles(noOfFiles);

        return instance;
    }

}
