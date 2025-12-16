package com.grabbill.server.controller.request;

import com.grabbill.core.entity.EmailCampaignActivity;
import com.grabbill.core.model.ProcessStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class EmailCampaignActivityRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String emailFrom;

    @Size(max = 255)
    private String emailFromName;

    @Size(max = 255)
    private String emailSubject;

    private String emailContent;
    private String emailMjmlContent;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime scheduledTimestamp;

    @NotNull
    private ProcessStatus status;

    private Integer contactGroupId;


    public void to(final EmailCampaignActivity emailCampaignActivity) {
        emailCampaignActivity.setName(this.name);
        emailCampaignActivity.setEmailFrom(this.emailFrom);
        emailCampaignActivity.setEmailFromName(this.emailFromName);
        emailCampaignActivity.setEmailSubject(this.emailSubject);
        emailCampaignActivity.setEmailContent(this.emailContent);
        emailCampaignActivity.setEmailMjmlContent(this.emailMjmlContent);
        emailCampaignActivity.setStatus(this.status);

        if (scheduledTimestamp != null) {
            emailCampaignActivity.setScheduledTimestamp(scheduledTimestamp);
        }
    }

}
