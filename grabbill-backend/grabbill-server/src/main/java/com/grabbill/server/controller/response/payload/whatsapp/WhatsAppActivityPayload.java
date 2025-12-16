package com.grabbill.server.controller.response.payload.whatsapp;

import com.grabbill.core.entity.*;
import com.grabbill.server.controller.response.payload.*;
import lombok.Data;

import javax.persistence.Column;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author seez
 */
@Data
public class WhatsAppActivityPayload extends BaseActivityPayload {


    OffsetDateTime scheduledTimestamp;

    private String whatsappTemplateName;
    private String whatsappBodyContent;
    private boolean whatsappDocument;
    private String whatsappFooterContent;
    private String whatsappButton;

    private int whatsAppStatusSent;
    private int whatsAppStatusSkip;
    private int whatsAppStatusRead;
    private int whatsAppStatusDelivered;
    private int whatsAppStatusAcknowledge;
    private int whatsAppStatusFailed;


    public static WhatsAppActivityPayload from(final WhatsAppActivity whatsAppActivity) {
        WhatsAppActivityPayload instance = new WhatsAppActivityPayload();
        instance.setId(whatsAppActivity.getId());instance.setScheduledTimestamp(whatsAppActivity.getScheduledTimestamp());
        instance.setWhatsappTemplateName(whatsAppActivity.getWhatsAppTemplateName());
        instance.setWhatsappDocument(whatsAppActivity.getWhatsAppDocument() != null && whatsAppActivity.getWhatsAppDocument());
        instance.setWhatsappBodyContent(whatsAppActivity.getWhatsAppBodyContent());
        instance.setWhatsappFooterContent(whatsAppActivity.getWhatsAppFooterContent());
        instance.setWhatsappButton(whatsAppActivity.getWhatsAppButton());
        instance.setWhatsAppStatusSent(whatsAppActivity.getWhatsAppStatusSent());
        instance.setWhatsAppStatusSkip(whatsAppActivity.getWhatsAppStatusSkip());
        instance.setWhatsAppStatusRead(whatsAppActivity.getWhatsAppStatusRead());
        instance.setWhatsAppStatusDelivered(whatsAppActivity.getWhatsAppStatusDelivered());
        instance.setWhatsAppStatusAcknowledge(whatsAppActivity.getWhatsAppStatusAcknowledge());
        instance.setWhatsAppStatusFailed(whatsAppActivity.getWhatsAppStatusFailed());

        instance.copyFrom(whatsAppActivity);

        List<BaseFilePayload> files = new ArrayList<>();
        for (WhatsAppFile whatsAppFile : whatsAppActivity.getWhatsAppFiles()) {
            files.add(toBaseFilePayload(whatsAppFile));
        }
        instance.setFiles(files);

        List<BaseIndexRowPayload> indexRows = new ArrayList<>();
        for (WhatsAppIndexRow indexRow : whatsAppActivity.getWhatsAppIndexRows()) {
            indexRows.add(toBaseIndexRowPayload(indexRow));
        }
        instance.setIndexRows(indexRows);

        List<BaseRecordPayload> records = new ArrayList<>();
        for (WhatsAppRecord record : whatsAppActivity.getWhatsAppRecords()) {
            WhatsAppRecordPayload recordPayload = new WhatsAppRecordPayload();
            recordPayload.copyFrom(record);
            recordPayload.setIndexRow(toBaseIndexRowPayload(record.getWhatsAppIndexRow()));
            records.add(recordPayload);
        }
        instance.setRecords(records);

        return instance;
    }

    private static BaseFilePayload toBaseFilePayload(final WhatsAppFile whatsAppFile) {
        BaseFilePayload filePayload = new BaseFilePayload();
        filePayload.setId(whatsAppFile.getId());
        filePayload.copyFrom(whatsAppFile);
        return filePayload;
    }

    private static BaseIndexRowPayload toBaseIndexRowPayload(final WhatsAppIndexRow indexRow) {
        BaseIndexRowPayload indexRowPayload = new BaseIndexRowPayload();
        indexRowPayload.setId(indexRow.getId());
        indexRowPayload.copyFrom(indexRow);
        indexRowPayload.setFile(indexRow.getWhatsAppFile() == null ? null : toBaseFilePayload(indexRow.getWhatsAppFile()));
        return indexRowPayload;
    }

}
