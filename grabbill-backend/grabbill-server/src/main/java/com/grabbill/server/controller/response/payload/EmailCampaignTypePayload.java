package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class EmailCampaignTypePayload extends BaseTypePayload {

    private String emailFrom;
    private String emailFromName;
    private String emailSubject;
    private String emailContent;
    private String emailMjmlContent;
    private boolean hasAttachment;

    private int emailTotal;
    private int emailStatusSent;
    private int emailStatusUnsubscribed;
    private int emailStatusUnsubscribedSkip;
    private int emailStatusBounced;
    private int emailStatusBouncedSkip;
    private int emailStatusOpened;

    private Integer contactGroupId;
    private String contactGroupName;

    boolean autoPurge;
    Integer autoPurgeByDays;


    public static EmailCampaignTypePayload from(
            final EmailCampaignType emailCampaignType,
            final List<EmailCampaignActivity> emailCampaignActivities,
            final int totalUnsubscribed
    ) {
        EmailCampaignTypePayload instance = new EmailCampaignTypePayload();
        instance.setId(emailCampaignType.getId());
        instance.copyFrom(emailCampaignType);
        instance.setEmailFrom(emailCampaignType.getEmailFrom());
        instance.setEmailFromName(emailCampaignType.getEmailFromName());
        instance.setEmailSubject(emailCampaignType.getEmailSubject());
        instance.setEmailContent(emailCampaignType.getEmailContent());
        instance.setEmailMjmlContent(emailCampaignType.getEmailMjmlContent());
        instance.setHasAttachment(emailCampaignType.isHasAttachment());
        instance.setAutoPurge(emailCampaignType.isAutoPurge());
        instance.setAutoPurgeByDays(emailCampaignType.getAutoPurgeByDays());

        if (emailCampaignActivities != null && !emailCampaignActivities.isEmpty()) {
            int noOfFiles = 0;
            int emailTotal = 0;
            int emailStatusSent = 0;
            int emailStatusUnsubscribedSkip = 0;
            int emailStatusBounced = 0;
            int emailStatusBouncedSkip = 0;
            int emailStatusOpened = 0;
            for (EmailCampaignActivity emailCampaignActivity : emailCampaignActivities) {
                noOfFiles += emailCampaignActivity.getEmailCampaignFiles().size();

                emailTotal += emailCampaignActivity.getEmailCampaignIndexRows().size();
                emailStatusSent += emailCampaignActivity.getEmailStatusSent();
                emailStatusUnsubscribedSkip += emailCampaignActivity.getEmailStatusUnsubscribedSkip();
                emailStatusBounced += emailCampaignActivity.getEmailStatusBounced();
                emailStatusBouncedSkip += emailCampaignActivity.getEmailStatusBouncedSkip() == null ? 0 : emailCampaignActivity.getEmailStatusBouncedSkip();
                emailStatusOpened += emailCampaignActivity.getEmailStatusOpened();
            }

            instance.setNoOfFiles(noOfFiles);
            instance.setEmailTotal(emailTotal);
            instance.setEmailStatusSent(emailStatusSent);
            instance.setEmailStatusUnsubscribed(totalUnsubscribed);
            instance.setEmailStatusUnsubscribedSkip(emailStatusUnsubscribedSkip);
            instance.setEmailStatusBounced(emailStatusBounced);
            instance.setEmailStatusBouncedSkip(emailStatusBouncedSkip);
            instance.setEmailStatusOpened(emailStatusOpened);
        }

        ContactGroup contactGroup = emailCampaignType.getContactGroup();
        if (contactGroup != null) {
            instance.setContactGroupId(contactGroup.getId());
            instance.setContactGroupName(contactGroup.getName());
        }

        // TODO: revise this
        List<BaseIndexFieldPayload> indexFields = new ArrayList<>();
//        for (TransactionalEmailIndexField transactionalEmailIndexField : emailCampaignType.getTransactionalEmailIndexFields()) {
//            BaseIndexFieldPayload indexFieldPayload = new BaseIndexFieldPayload();
//            indexFieldPayload.setId(transactionalEmailIndexField.getId());
//            indexFieldPayload.copyFrom(transactionalEmailIndexField);
//            indexFields.add(indexFieldPayload);
//        }
        instance.setIndexFields(indexFields);

        return instance;
    }

}
