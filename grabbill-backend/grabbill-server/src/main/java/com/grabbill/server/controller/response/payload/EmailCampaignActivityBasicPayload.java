package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.EmailCampaignActivity;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class EmailCampaignActivityBasicPayload extends BaseActivityBasicPayload {

    public static EmailCampaignActivityBasicPayload from(
            final EmailCampaignActivity emailCampaignActivity
    ) {
        EmailCampaignActivityBasicPayload instance = new EmailCampaignActivityBasicPayload();
        instance.setId(emailCampaignActivity.getId());
        instance.setNoOfFiles(emailCampaignActivity.getEmailCampaignFiles().size());
        instance.copyFrom(emailCampaignActivity);

        return instance;
    }

}
