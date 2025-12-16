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
public class MTWhatsAppActivityPayload extends BaseActivityPayload {


    private OffsetDateTime scheduledTimestamp;
    private List<MTWhatsAppActivityTemplatePayload> whatsAppActivityTemplates;

    private boolean sftp;
    private String sftpPath;

    private int whatsAppStatusSent;
    private int whatsAppStatusSkip;
    private int whatsAppStatusRead;
    private int whatsAppStatusDelivered;
    private int whatsAppStatusAcknowledge;
    private int whatsAppStatusFailed;


    public static MTWhatsAppActivityPayload from(final MTWhatsAppActivity mtWhatsAppActivity) {
        MTWhatsAppActivityPayload instance = new MTWhatsAppActivityPayload();
        instance.setId(mtWhatsAppActivity.getId());instance.setScheduledTimestamp(mtWhatsAppActivity.getScheduledTimestamp());

        List<MTWhatsAppActivityTemplatePayload> templates = new ArrayList<>();
        for (MTWhatsAppActivityTemplate template : mtWhatsAppActivity.getMtWhatsAppActivityTemplates()) {
            templates.add(MTWhatsAppActivityTemplatePayload.from(template));
        }
        instance.setWhatsAppActivityTemplates(templates);

        instance.setSftp(mtWhatsAppActivity.isSftp());
        instance.setSftpPath(mtWhatsAppActivity.getSftpPath());

        instance.setWhatsAppStatusSent(mtWhatsAppActivity.getWhatsAppStatusSent());
        instance.setWhatsAppStatusSkip(mtWhatsAppActivity.getWhatsAppStatusSkip());
        instance.setWhatsAppStatusRead(mtWhatsAppActivity.getWhatsAppStatusRead());
        instance.setWhatsAppStatusDelivered(mtWhatsAppActivity.getWhatsAppStatusDelivered());
        instance.setWhatsAppStatusAcknowledge(mtWhatsAppActivity.getWhatsAppStatusAcknowledge());
        instance.setWhatsAppStatusFailed(mtWhatsAppActivity.getWhatsAppStatusFailed());

        instance.copyFrom(mtWhatsAppActivity);

        List<BaseFilePayload> files = new ArrayList<>();
        for (MTWhatsAppFile whatsAppFile : mtWhatsAppActivity.getMtWhatsAppFiles()) {
            files.add(toBaseFilePayload(whatsAppFile));
        }
        instance.setFiles(files);

        List<BaseIndexRowPayload> indexRows = new ArrayList<>();
        for (MTWhatsAppIndexRow indexRow : mtWhatsAppActivity.getMtWhatsAppIndexRows()) {
            indexRows.add(toBaseIndexRowPayload(indexRow));
        }
        instance.setIndexRows(indexRows);

        List<BaseRecordPayload> records = new ArrayList<>();
        for (MTWhatsAppRecord record : mtWhatsAppActivity.getMtWhatsAppRecords()) {
            MTWhatsAppRecordPayload recordPayload = new MTWhatsAppRecordPayload();
            recordPayload.copyFrom(record);
            recordPayload.setIndexRow(toBaseIndexRowPayload(record.getMtWhatsAppIndexRow()));
            records.add(recordPayload);
        }
        instance.setRecords(records);

        return instance;
    }

    private static BaseFilePayload toBaseFilePayload(final MTWhatsAppFile mtWhatsAppFile) {
        BaseFilePayload filePayload = new BaseFilePayload();
        filePayload.setId(mtWhatsAppFile.getId());
        filePayload.copyFrom(mtWhatsAppFile);
        return filePayload;
    }

    private static BaseIndexRowPayload toBaseIndexRowPayload(final MTWhatsAppIndexRow indexRow) {
        BaseIndexRowPayload indexRowPayload = new BaseIndexRowPayload();
        indexRowPayload.setId(indexRow.getId());
        indexRowPayload.copyFrom(indexRow);
        indexRowPayload.setFile(indexRow.getMtWhatsAppFile() == null ? null : toBaseFilePayload(indexRow.getMtWhatsAppFile()));
        return indexRowPayload;
    }

}
