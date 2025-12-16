package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class MTTransactionalEmailTypePayload extends BaseTypePayload {

    private String emailFrom;
    private String emailFromName;
    private String emailAttachmentFileName;
    private boolean sendSms;
    private String smsContent;
    private boolean passwordProtected;
    private boolean hasAttachment;
    private boolean archive;
    private String csvSeparator;

    private int emailTotal;
    private int emailStatusSent;
    private int emailStatusUnsubscribed;
    private int emailStatusUnsubscribedSkip;
    private int emailStatusBounced;
    private int emailStatusBouncedSkip;
    private int emailStatusOpened;

    private boolean autoPurge;
    private Integer autoPurgeByDays;

    private String whatsAppTemplateName;
    private List<MTWhatsappTemplateParamPayload> whatsAppTemplateParams;

    private List<MTTransactionalEmailTemplatePayload> transactionalEmailTemplates;


    public static MTTransactionalEmailTypePayload from(
            final MTTransactionalEmailType mtTransactionalEmailType,
            final List<MTTransactionalEmailActivity> mtTransactionalEmailActivities,
            final int totalUnsubscribed
    ) {
        MTTransactionalEmailTypePayload instance = new MTTransactionalEmailTypePayload();
        instance.setId(mtTransactionalEmailType.getId());
        instance.copyFrom(mtTransactionalEmailType);
        instance.setEmailFrom(mtTransactionalEmailType.getEmailFrom());
        instance.setEmailFromName(mtTransactionalEmailType.getEmailFromName());
        instance.setEmailAttachmentFileName(mtTransactionalEmailType.getEmailAttachmentFileName());
        instance.setSendSms(mtTransactionalEmailType.isSendSms());
        instance.setSmsContent(mtTransactionalEmailType.getSmsContent());
        instance.setPasswordProtected(mtTransactionalEmailType.isPasswordProtected());
        instance.setHasAttachment(mtTransactionalEmailType.isHasAttachment());
        instance.setArchive(mtTransactionalEmailType.isArchive());
        instance.setCsvSeparator(mtTransactionalEmailType.getCsvSeparator());
        instance.setAutoPurge(mtTransactionalEmailType.isAutoPurge());
        instance.setAutoPurgeByDays(mtTransactionalEmailType.getAutoPurgeByDays());
        instance.setWhatsAppTemplateName(mtTransactionalEmailType.getWhatsAppTemplateName());

        if (mtTransactionalEmailActivities != null && !mtTransactionalEmailActivities.isEmpty()) {
            int noOfFiles = 0;
            int emailTotal = 0;
            int emailStatusSent = 0;
            int emailStatusUnsubscribedSkip = 0;
            int emailStatusBounced = 0;
            int emailStatusBouncedSkip = 0;
            int emailStatusOpened = 0;
            for (MTTransactionalEmailActivity mtTransactionalEmailActivity : mtTransactionalEmailActivities) {
                noOfFiles += mtTransactionalEmailActivity.getMtTransactionalEmailFiles().size();

                emailTotal += mtTransactionalEmailActivity.getMtTransactionalEmailIndexRows().size();
                emailStatusSent += mtTransactionalEmailActivity.getEmailStatusSent();
                emailStatusUnsubscribedSkip += mtTransactionalEmailActivity.getEmailStatusUnsubscribedSkip();
                emailStatusBounced += mtTransactionalEmailActivity.getEmailStatusBounced();
                emailStatusBouncedSkip += mtTransactionalEmailActivity.getEmailStatusBouncedSkip() == null ? 0 : mtTransactionalEmailActivity.getEmailStatusBouncedSkip();
                emailStatusOpened += mtTransactionalEmailActivity.getEmailStatusOpened();
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

        List<BaseIndexFieldPayload> indexFields = new ArrayList<>();
        for (MTTransactionalEmailIndexField mtTransactionalEmailIndexField : mtTransactionalEmailType.getMtTransactionalEmailIndexFields()) {
            BaseIndexFieldPayload indexFieldPayload = new BaseIndexFieldPayload();
            indexFieldPayload.setId(mtTransactionalEmailIndexField.getId());
            indexFieldPayload.copyFrom(mtTransactionalEmailIndexField);
            indexFields.add(indexFieldPayload);
        }
        instance.setIndexFields(indexFields);


        List<MTWhatsappTemplateParamPayload> params = new ArrayList<>();
        for (MTTransactionalEmailWhatsappTemplateParam mtTransactionalEmailParam : mtTransactionalEmailType.getMtTransactionalEmailWhatsappTemplateParams()) {
            MTWhatsappTemplateParamPayload paramPayload = new MTWhatsappTemplateParamPayload();
            paramPayload.setId(mtTransactionalEmailParam.getId());
            paramPayload.copyFrom(mtTransactionalEmailParam);
            params.add(paramPayload);
        }
        instance.setWhatsAppTemplateParams(params);

        List<MTTransactionalEmailTemplatePayload> templates = new ArrayList<>();
        for (MTTransactionalEmailTemplate mtTransactionalEmailTemplate : mtTransactionalEmailType.getMtTransactionalEmailTemplates()) {
            templates.add(MTTransactionalEmailTemplatePayload.from(mtTransactionalEmailTemplate));
        }
        instance.setTransactionalEmailTemplates(templates);

        return instance;
    }

}
