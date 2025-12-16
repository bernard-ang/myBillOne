package com.grabbill.server.controller.request;

import com.grabbill.core.entity.EmailCampaignType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author michaellow
 */
@Data
public class EmailCampaignTypeRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String code;

    @Size(max = 255)
    private String emailFrom;

    @Size(max = 255)
    private String emailFromName;

    @Size(max = 255)
    private String emailSubject;

    private String emailContent;

    private String emailMjmlContent;

    private boolean hasAttachment;

    private boolean autoPurge;

    private Integer autoPurgeByDays;

    private Integer contactGroupId;


    public void to(final EmailCampaignType emailCampaignType) {
        emailCampaignType.setName(this.name);
        emailCampaignType.setCode(this.code);
        emailCampaignType.setEmailFrom(this.emailFrom);
        emailCampaignType.setEmailFromName(this.emailFromName);
        emailCampaignType.setEmailSubject(this.emailSubject);
        emailCampaignType.setEmailContent(this.emailContent);
        emailCampaignType.setEmailMjmlContent(this.emailMjmlContent);
        emailCampaignType.setHasAttachment(this.hasAttachment);
        emailCampaignType.setAutoPurge(this.autoPurge);
        emailCampaignType.setAutoPurgeByDays(this.autoPurgeByDays);
        emailCampaignType.setContactGroup(emailCampaignType.getContactGroup());
    }

}
