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
public class MTTransactionalEmailActivityPayload extends BaseActivityPayload {

    private boolean sftp;
    private String sftpPath;

    private String emailFrom;
    private String emailFromName;
    private String emailAttachmentFileName;
    private String smsContent;

    private int emailTotal;
    private int emailStatusSent;
    private int emailStatusUnsubscribedSkip;
    private int emailStatusUnsubscribed;
    private int emailStatusBounced;
    private int emailStatusBouncedSkip;
    private int emailStatusOpened;

    OffsetDateTime scheduledTimestamp;

    private boolean sendWhatsAppMessage;
    private String whatsAppTemplateName;
    private String whatsAppBodyContent;
    private boolean whatsAppDocument;
    private String whatsAppFooterContent;
    private String whatsAppButton;

    private List<EmbeddedLinkPayload> embeddedLinks = new ArrayList<>();
    private List<MTTransactionalEmailActivityTemplatePayload> mtTransactionalEmailActivityTemplates = new ArrayList<>();


    public static MTTransactionalEmailActivityPayload from(
            final MTTransactionalEmailActivity mtTransactionalEmailActivity,
            final List<EmbeddedLinkPayload> embeddedLinks,
            final int totalUnsubscribed
    ) {
        MTTransactionalEmailActivityPayload instance = from(mtTransactionalEmailActivity);
        instance.embeddedLinks = embeddedLinks;
        instance.emailStatusUnsubscribed = totalUnsubscribed;

        return instance;
    }

    public static MTTransactionalEmailActivityPayload from(final MTTransactionalEmailActivity mtTransactionalEmailActivity) {
        MTTransactionalEmailActivityPayload instance = new MTTransactionalEmailActivityPayload();
        instance.setSftp(mtTransactionalEmailActivity.isSftp());
        instance.setSftpPath(mtTransactionalEmailActivity.getSftpPath());

        instance.setId(mtTransactionalEmailActivity.getId());
        instance.setEmailFrom(mtTransactionalEmailActivity.getEmailFrom());
        instance.setEmailFromName(mtTransactionalEmailActivity.getEmailFromName());
        instance.setEmailAttachmentFileName(mtTransactionalEmailActivity.getEmailAttachmentFileName());
        instance.setSmsContent(mtTransactionalEmailActivity.getSmsContent());
        instance.setEmailTotal(mtTransactionalEmailActivity.getMtTransactionalEmailIndexRows().size());
        instance.setEmailStatusSent(mtTransactionalEmailActivity.getEmailStatusSent());
        instance.setEmailStatusOpened(mtTransactionalEmailActivity.getEmailStatusOpened());
        instance.setEmailStatusBounced(mtTransactionalEmailActivity.getEmailStatusBounced());
        instance.setEmailStatusUnsubscribedSkip(mtTransactionalEmailActivity.getEmailStatusUnsubscribedSkip());
        instance.setEmailStatusBouncedSkip(mtTransactionalEmailActivity.getEmailStatusBouncedSkip() == null ? 0 : mtTransactionalEmailActivity.getEmailStatusBouncedSkip());
        instance.setScheduledTimestamp(mtTransactionalEmailActivity.getScheduledTimestamp());

        instance.setSendWhatsAppMessage(mtTransactionalEmailActivity.getSendWhatsAppMessage() != null && mtTransactionalEmailActivity.getSendWhatsAppMessage());
        instance.setWhatsAppTemplateName(mtTransactionalEmailActivity.getWhatsAppTemplateName());
        instance.setWhatsAppDocument(mtTransactionalEmailActivity.getWhatsAppDocument() != null && mtTransactionalEmailActivity.getWhatsAppDocument());
        instance.setWhatsAppBodyContent(mtTransactionalEmailActivity.getWhatsAppBodyContent());
        instance.setWhatsAppFooterContent(mtTransactionalEmailActivity.getWhatsAppFooterContent());
        instance.setWhatsAppButton(mtTransactionalEmailActivity.getWhatsAppButton());

        instance.copyFrom(mtTransactionalEmailActivity);

        List<BaseFilePayload> files = new ArrayList<>();
        for (MTTransactionalEmailFile transactionalEmailFile : mtTransactionalEmailActivity.getMtTransactionalEmailFiles()) {
            files.add(toBaseFilePayload(transactionalEmailFile));
        }
        instance.setFiles(files);

        List<BaseIndexRowPayload> indexRows = new ArrayList<>();
        for (MTTransactionalEmailIndexRow indexRow : mtTransactionalEmailActivity.getMtTransactionalEmailIndexRows()) {
            indexRows.add(toBaseIndexRowPayload(indexRow));
        }
        instance.setIndexRows(indexRows);

        List<BaseRecordPayload> records = new ArrayList<>();
        for (MTTransactionalEmailRecord record : mtTransactionalEmailActivity.getMtTransactionalEmailRecords()) {
            MTTransactionalEmailRecordPayload recordPayload = new MTTransactionalEmailRecordPayload();
            recordPayload.copyFrom(record);
            recordPayload.setIndexRow(toBaseIndexRowPayload(record.getMtTransactionalEmailIndexRow()));
            records.add(recordPayload);
        }
        instance.setRecords(records);

        List<MTTransactionalEmailActivityTemplatePayload> templates = new ArrayList<>();
        for (MTTransactionalEmailActivityTemplate mtTransactionalEmailActivityTemplate : mtTransactionalEmailActivity.getMtTransactionalEmailActivityTemplates()) {
            templates.add(MTTransactionalEmailActivityTemplatePayload.from(mtTransactionalEmailActivityTemplate));
        }
        instance.setMtTransactionalEmailActivityTemplates(templates);

        return instance;
    }

    private static BaseFilePayload toBaseFilePayload(final MTTransactionalEmailFile mtTransactionalEmailFile) {
        BaseFilePayload filePayload = new BaseFilePayload();
        filePayload.setId(mtTransactionalEmailFile.getId());
        filePayload.copyFrom(mtTransactionalEmailFile);
        return filePayload;
    }

    private static BaseIndexRowPayload toBaseIndexRowPayload(final MTTransactionalEmailIndexRow indexRow) {
        BaseIndexRowPayload indexRowPayload = new BaseIndexRowPayload();
        indexRowPayload.setId(indexRow.getId());
        indexRowPayload.copyFrom(indexRow);
        indexRowPayload.setFile(indexRow.getMtTransactionalEmailFile() == null
                ? null : toBaseFilePayload(indexRow.getMtTransactionalEmailFile()));
        return indexRowPayload;
    }

}
