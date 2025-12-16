package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class EmailCampaignActivityPayload extends BaseActivityPayload {

    private String emailFrom;
    private String emailFromName;
    private String emailSubject;
    private String emailContent;
    private String emailMjmlContent;

    private int emailTotal;
    private int emailStatusSent;
    private int emailStatusUnsubscribedSkip;
    private int emailStatusUnsubscribed;
    private int emailStatusBounced;
    private int emailStatusBouncedSkip;
    private int emailStatusOpened;

    private Integer contactGroupId;
    private String contactGroupName;

    OffsetDateTime scheduledTimestamp;

    private List<EmbeddedLinkPayload> embeddedLinks = new ArrayList<>();


    public static EmailCampaignActivityPayload from(
            final EmailCampaignActivity emailCampaignActivity,
            final List<EmbeddedLinkPayload> embeddedLinks,
            final int totalUnsubscribed
    ) {
        EmailCampaignActivityPayload instance = from(emailCampaignActivity);
        instance.embeddedLinks = embeddedLinks;
        instance.emailStatusUnsubscribed = totalUnsubscribed;

        return instance;
    }

    public static EmailCampaignActivityPayload from(final EmailCampaignActivity emailCampaignActivity) {
        EmailCampaignActivityPayload instance = new EmailCampaignActivityPayload();
        instance.setId(emailCampaignActivity.getId());
        instance.setEmailFrom(emailCampaignActivity.getEmailFrom());
        instance.setEmailFromName(emailCampaignActivity.getEmailFromName());
        instance.setEmailSubject(emailCampaignActivity.getEmailSubject());
        instance.setEmailContent(emailCampaignActivity.getEmailContent());
        instance.setEmailMjmlContent(emailCampaignActivity.getEmailMjmlContent());
        instance.setEmailTotal(emailCampaignActivity.getEmailCampaignIndexRows().size());
        instance.setEmailStatusSent(emailCampaignActivity.getEmailStatusSent());
        instance.setEmailStatusOpened(emailCampaignActivity.getEmailStatusOpened());
        instance.setEmailStatusBounced(emailCampaignActivity.getEmailStatusBounced());
        instance.setEmailStatusUnsubscribedSkip(emailCampaignActivity.getEmailStatusUnsubscribedSkip());
        instance.setEmailStatusBouncedSkip(emailCampaignActivity.getEmailStatusBouncedSkip() == null ? 0 : emailCampaignActivity.getEmailStatusBouncedSkip());
        instance.setScheduledTimestamp(emailCampaignActivity.getScheduledTimestamp());
        instance.copyFrom(emailCampaignActivity);

        ContactGroup contactGroup = emailCampaignActivity.getContactGroup();
        if (contactGroup != null) {
            instance.setContactGroupId(contactGroup.getId());
            instance.setContactGroupName(contactGroup.getName());
        }

        List<BaseFilePayload> files = new ArrayList<>();
        for (EmailCampaignFile file : emailCampaignActivity.getEmailCampaignFiles()) {
            files.add(toBaseFilePayload(file));
        }
        instance.setFiles(files);

        List<BaseIndexRowPayload> indexRows = new ArrayList<>();
        for (EmailCampaignIndexRow indexRow : emailCampaignActivity.getEmailCampaignIndexRows()) {
            indexRows.add(toBaseIndexRowPayload(indexRow));
        }
        instance.setIndexRows(indexRows);

        List<BaseRecordPayload> records = new ArrayList<>();
        for (EmailCampaignRecord record : emailCampaignActivity.getEmailCampaignRecords()) {
            EmailCampaignRecordPayload recordPayload = new EmailCampaignRecordPayload();
            recordPayload.copyFrom(record);
            recordPayload.setIndexRow(toBaseIndexRowPayload(record.getEmailCampaignIndexRow()));
            records.add(recordPayload);
        }
        instance.setRecords(records);

        return instance;
    }

    private static BaseFilePayload toBaseFilePayload(final EmailCampaignFile emailCampaignFile) {
        BaseFilePayload filePayload = new BaseFilePayload();
        filePayload.setId(emailCampaignFile.getId());
        filePayload.copyFrom(emailCampaignFile);
        return filePayload;
    }

    private static BaseIndexRowPayload toBaseIndexRowPayload(final EmailCampaignIndexRow indexRow) {
        BaseIndexRowPayload indexRowPayload = new BaseIndexRowPayload();
        indexRowPayload.setId(indexRow.getId());
        indexRowPayload.copyFrom(indexRow);
        return indexRowPayload;
    }

}
