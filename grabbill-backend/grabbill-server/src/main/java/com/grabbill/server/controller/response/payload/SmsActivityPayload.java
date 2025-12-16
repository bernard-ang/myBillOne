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
public class SmsActivityPayload extends BaseActivityPayload {

    private String smsFrom;
    private String smsContent;

    private int totalSms;
    private int smsStatusSent;
    private int smsCreditUsed;
    private int smsStatusError;
    OffsetDateTime scheduledTimestamp;

    private Integer contactGroupId;
    private String contactGroupName;


    public static SmsActivityPayload from(final SmsActivity smsActivity) {
        SmsActivityPayload instance = new SmsActivityPayload();
        instance.setId(smsActivity.getId());
        instance.setSmsFrom(smsActivity.getSmsFrom());
        instance.setSmsContent(smsActivity.getSmsContent());
        instance.setTotalSms(smsActivity.getTotalSms());
        instance.setSmsStatusError(smsActivity.getSmsStatusError());
        instance.setSmsStatusSent(smsActivity.getSmsStatusSent());
        instance.setSmsCreditUsed(smsActivity.getSmsCreditUsed());
        instance.setScheduledTimestamp(smsActivity.getScheduledTimestamp());
        instance.copyFrom(smsActivity);

        instance.setFiles(new ArrayList<>());

        ContactGroup contactGroup = smsActivity.getContactGroup();
        if (contactGroup != null) {
            instance.setContactGroupId(contactGroup.getId());
            instance.setContactGroupName(contactGroup.getName());
        }

        List<BaseIndexRowPayload> indexRows = new ArrayList<>();
        for (SmsIndexRow indexRow : smsActivity.getSmsIndexRows()) {
            indexRows.add(toBaseIndexRowPayload(indexRow));
        }
        instance.setIndexRows(indexRows);

        List<BaseRecordPayload> records = new ArrayList<>();
        for (SmsRecord record : smsActivity.getSmsRecords()) {
            SmsRecordPayload recordPayload = new SmsRecordPayload();
            recordPayload.copyFrom(record);
            recordPayload.setIndexRow(record.getSmsIndexRow() != null ? toBaseIndexRowPayload(record.getSmsIndexRow()) : null);
            records.add(recordPayload);
        }
        instance.setRecords(records);

        return instance;
    }

    private static BaseIndexRowPayload toBaseIndexRowPayload(final SmsIndexRow indexRow) {
        BaseIndexRowPayload indexRowPayload = new BaseIndexRowPayload();
        indexRowPayload.setId(indexRow.getId());
        indexRowPayload.copyFrom(indexRow);

        return indexRowPayload;
    }

}
