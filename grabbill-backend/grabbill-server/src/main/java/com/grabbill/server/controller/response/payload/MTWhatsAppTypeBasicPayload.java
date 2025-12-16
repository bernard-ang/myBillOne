package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppType;
import com.grabbill.core.repository.MTWhatsAppFileRepository;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class MTWhatsAppTypeBasicPayload extends BaseTypeBasicPayload {

    private String lastSentBy;

    private OffsetDateTime lastSentDate;

    private boolean hasAttachment;

    private boolean passwordProtected;

    public static MTWhatsAppTypeBasicPayload from (
            final MTWhatsAppType type,
            final List<MTWhatsAppActivity> activities,
            final MTWhatsAppFileRepository mtWhatsAppFileRepository
    ) {
        MTWhatsAppTypeBasicPayload instance = new MTWhatsAppTypeBasicPayload();
        instance.setId(type.getId());
        instance.setLastSentBy(type.getLastSentBy());
        instance.setLastSentDate(type.getLastSentDate());
        instance.setHasAttachment(type.isHasAttachment());
        instance.setPasswordProtected(type.isPasswordProtected());
        instance.copyFrom(type);

        int noOfFiles = 0;
        int batchSize = 500;
        for (int i = 0; i < activities.size(); i += batchSize) {
            int end = Math.min(i + batchSize, activities.size());
            List<MTWhatsAppActivity> sublist = activities.subList(i, end);
            noOfFiles += mtWhatsAppFileRepository.countByMtWhatsAppActivityIn(sublist);
        }
        instance.setNoOfFiles(noOfFiles);

        return instance;
    }

}
