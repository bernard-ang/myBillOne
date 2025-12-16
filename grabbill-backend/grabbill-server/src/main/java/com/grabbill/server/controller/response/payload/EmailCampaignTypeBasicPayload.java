package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.EmailCampaignActivity;
import com.grabbill.core.entity.EmailCampaignType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class EmailCampaignTypeBasicPayload extends BaseTypeBasicPayload {

    private String lastSentBy;

    private OffsetDateTime lastSentDate;

    private boolean hasAttachment;


    public static EmailCampaignTypeBasicPayload from (
            final EmailCampaignType type,
            final List<EmailCampaignActivity> activities
    ) {
        EmailCampaignTypeBasicPayload instance = new EmailCampaignTypeBasicPayload();
        instance.setId(type.getId());
        instance.setLastSentBy(type.getLastSentBy());
        instance.setLastSentDate(type.getLastSentDate());
        instance.setHasAttachment(type.isHasAttachment());
        instance.copyFrom(type);

        int noOfFiles = 0;
        for (EmailCampaignActivity activity : activities) {
            noOfFiles += activity.getEmailCampaignFiles().size();
        }
        instance.setNoOfFiles(noOfFiles);

        return instance;
    }

}
