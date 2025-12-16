package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailIndexField;
import com.grabbill.core.entity.TransactionalEmailType;
import com.grabbill.core.entity.TransactionalEmailWhatsappTemplateParam;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class TransactionalEmailTypePayload extends BaseTypePayload {

    private String emailFrom;
    private String emailFromName;
    private String emailSubject;
    private String emailContent;
    private String emailMjmlContent;
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

    boolean autoPurge;
    Integer autoPurgeByDays;

    private String whatsAppTemplateName;
    List<WhatsappTemplateParamPayload> whatsAppTemplateParams;


    public static TransactionalEmailTypePayload from(
            final TransactionalEmailType transactionalEmailType,
            final List<TransactionalEmailActivity> transactionalEmailActivities,
            final int totalUnsubscribed
    ) {
        TransactionalEmailTypePayload instance = new TransactionalEmailTypePayload();
        instance.setId(transactionalEmailType.getId());
        instance.copyFrom(transactionalEmailType);
        instance.setEmailFrom(transactionalEmailType.getEmailFrom());
        instance.setEmailFromName(transactionalEmailType.getEmailFromName());
        instance.setEmailSubject(transactionalEmailType.getEmailSubject());
        instance.setEmailContent(transactionalEmailType.getEmailContent());
        instance.setEmailMjmlContent(transactionalEmailType.getEmailMjmlContent());
        instance.setEmailAttachmentFileName(transactionalEmailType.getEmailAttachmentFileName());
        instance.setSendSms(transactionalEmailType.isSendSms());
        instance.setSmsContent(transactionalEmailType.getSmsContent());
        instance.setPasswordProtected(transactionalEmailType.isPasswordProtected());
        instance.setHasAttachment(transactionalEmailType.isHasAttachment());
        instance.setArchive(transactionalEmailType.isArchive());
        instance.setCsvSeparator(transactionalEmailType.getCsvSeparator());
        instance.setAutoPurge(transactionalEmailType.isAutoPurge());
        instance.setAutoPurgeByDays(transactionalEmailType.getAutoPurgeByDays());
        instance.setWhatsAppTemplateName(transactionalEmailType.getWhatsAppTemplateName());

        if (transactionalEmailActivities != null && !transactionalEmailActivities.isEmpty()) {
            int noOfFiles = 0;
            int emailTotal = 0;
            int emailStatusSent = 0;
            int emailStatusUnsubscribedSkip = 0;
            int emailStatusBounced = 0;
            int emailStatusBouncedSkip = 0;
            int emailStatusOpened = 0;
            for (TransactionalEmailActivity transactionalEmailActivity : transactionalEmailActivities) {
                noOfFiles += transactionalEmailActivity.getTransactionalEmailFiles().size();

                emailTotal += transactionalEmailActivity.getTransactionalEmailIndexRows().size();
                emailStatusSent += transactionalEmailActivity.getEmailStatusSent();
                emailStatusUnsubscribedSkip += transactionalEmailActivity.getEmailStatusUnsubscribedSkip();
                emailStatusBounced += transactionalEmailActivity.getEmailStatusBounced();
                emailStatusBouncedSkip += transactionalEmailActivity.getEmailStatusBouncedSkip() == null ? 0 : transactionalEmailActivity.getEmailStatusBouncedSkip();
                emailStatusOpened += transactionalEmailActivity.getEmailStatusOpened();
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
        for (TransactionalEmailIndexField transactionalEmailIndexField : transactionalEmailType.getTransactionalEmailIndexFields()) {
            BaseIndexFieldPayload indexFieldPayload = new BaseIndexFieldPayload();
            indexFieldPayload.setId(transactionalEmailIndexField.getId());
            indexFieldPayload.copyFrom(transactionalEmailIndexField);
            indexFields.add(indexFieldPayload);
        }
        instance.setIndexFields(indexFields);


        List<WhatsappTemplateParamPayload> params = new ArrayList<>();
        for (TransactionalEmailWhatsappTemplateParam transactionalEmailParam : transactionalEmailType.getTransactionalEmailWhatsappTemplateParams()) {
            WhatsappTemplateParamPayload paramPayload = new WhatsappTemplateParamPayload();
            paramPayload.setId(transactionalEmailParam.getId());
            paramPayload.copyFrom(transactionalEmailParam);
            params.add(paramPayload);
        }
        instance.setWhatsAppTemplateParams(params);

        return instance;
    }

}
