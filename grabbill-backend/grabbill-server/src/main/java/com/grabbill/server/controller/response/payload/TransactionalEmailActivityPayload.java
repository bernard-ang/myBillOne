package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailFile;
import com.grabbill.core.entity.TransactionalEmailIndexRow;
import com.grabbill.core.entity.TransactionalEmailRecord;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class TransactionalEmailActivityPayload extends BaseActivityPayload {

    private String emailFrom;
    private String emailFromName;
    private String emailSubject;
    private String emailContent;
    private String emailMjmlContent;
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


    public static TransactionalEmailActivityPayload from(
            final TransactionalEmailActivity transactionalEmailActivity,
            final List<EmbeddedLinkPayload> embeddedLinks,
            final int totalUnsubscribed
    ) {
        TransactionalEmailActivityPayload instance = from(transactionalEmailActivity);
        instance.embeddedLinks = embeddedLinks;
        instance.emailStatusUnsubscribed = totalUnsubscribed;

        return instance;
    }

    public static TransactionalEmailActivityPayload from(final TransactionalEmailActivity transactionalEmailActivity) {
        TransactionalEmailActivityPayload instance = new TransactionalEmailActivityPayload();
        instance.setId(transactionalEmailActivity.getId());
        instance.setEmailFrom(transactionalEmailActivity.getEmailFrom());
        instance.setEmailFromName(transactionalEmailActivity.getEmailFromName());
        instance.setEmailSubject(transactionalEmailActivity.getEmailSubject());
        instance.setEmailContent(transactionalEmailActivity.getEmailContent());
        instance.setEmailMjmlContent(transactionalEmailActivity.getEmailMjmlContent());
        instance.setEmailAttachmentFileName(transactionalEmailActivity.getEmailAttachmentFileName());
        instance.setSmsContent(transactionalEmailActivity.getSmsContent());
        instance.setEmailTotal(transactionalEmailActivity.getTransactionalEmailIndexRows().size());
        instance.setEmailStatusSent(transactionalEmailActivity.getEmailStatusSent());
        instance.setEmailStatusOpened(transactionalEmailActivity.getEmailStatusOpened());
        instance.setEmailStatusBounced(transactionalEmailActivity.getEmailStatusBounced());
        instance.setEmailStatusUnsubscribedSkip(transactionalEmailActivity.getEmailStatusUnsubscribedSkip());
        instance.setEmailStatusBouncedSkip(transactionalEmailActivity.getEmailStatusBouncedSkip() == null ? 0 : transactionalEmailActivity.getEmailStatusBouncedSkip());
        instance.setScheduledTimestamp(transactionalEmailActivity.getScheduledTimestamp());

        instance.setSendWhatsAppMessage(transactionalEmailActivity.getSendWhatsAppMessage() != null && transactionalEmailActivity.getSendWhatsAppMessage());
        instance.setWhatsAppTemplateName(transactionalEmailActivity.getWhatsAppTemplateName());
        instance.setWhatsAppDocument(transactionalEmailActivity.getWhatsAppDocument() != null && transactionalEmailActivity.getWhatsAppDocument());
        instance.setWhatsAppBodyContent(transactionalEmailActivity.getWhatsAppBodyContent());
        instance.setWhatsAppFooterContent(transactionalEmailActivity.getWhatsAppFooterContent());
        instance.setWhatsAppButton(transactionalEmailActivity.getWhatsAppButton());

        instance.copyFrom(transactionalEmailActivity);

        List<BaseFilePayload> files = new ArrayList<>();
        for (TransactionalEmailFile transactionalEmailFile : transactionalEmailActivity.getTransactionalEmailFiles()) {
            files.add(toBaseFilePayload(transactionalEmailFile));
        }
        instance.setFiles(files);

        List<BaseIndexRowPayload> indexRows = new ArrayList<>();
        for (TransactionalEmailIndexRow indexRow : transactionalEmailActivity.getTransactionalEmailIndexRows()) {
            indexRows.add(toBaseIndexRowPayload(indexRow));
        }
        instance.setIndexRows(indexRows);

        List<BaseRecordPayload> records = new ArrayList<>();
        for (TransactionalEmailRecord record : transactionalEmailActivity.getTransactionalEmailRecords()) {
            TransactionalEmailRecordPayload recordPayload = new TransactionalEmailRecordPayload();
            recordPayload.copyFrom(record);
            recordPayload.setIndexRow(toBaseIndexRowPayload(record.getTransactionalEmailIndexRow()));
            records.add(recordPayload);
        }
        instance.setRecords(records);

        return instance;
    }

    private static BaseFilePayload toBaseFilePayload(final TransactionalEmailFile transactionalEmailFile) {
        BaseFilePayload filePayload = new BaseFilePayload();
        filePayload.setId(transactionalEmailFile.getId());
        filePayload.copyFrom(transactionalEmailFile);
        return filePayload;
    }

    private static BaseIndexRowPayload toBaseIndexRowPayload(final TransactionalEmailIndexRow indexRow) {
        BaseIndexRowPayload indexRowPayload = new BaseIndexRowPayload();
        indexRowPayload.setId(indexRow.getId());
        indexRowPayload.copyFrom(indexRow);
        indexRowPayload.setFile(indexRow.getTransactionalEmailFile() == null
                ? null : toBaseFilePayload(indexRow.getTransactionalEmailFile()));
        return indexRowPayload;
    }

}
