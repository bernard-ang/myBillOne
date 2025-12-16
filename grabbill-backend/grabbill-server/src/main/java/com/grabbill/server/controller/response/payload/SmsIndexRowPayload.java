package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.SmsIndexRow;
import com.grabbill.core.entity.SmsRecord;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class SmsIndexRowPayload extends BaseIndexRowFullPayload {

    private String smsContent;


    public static SmsIndexRowPayload from(final SmsIndexRow indexRow) {
        SmsIndexRowPayload instance = new SmsIndexRowPayload();
        SmsRecord record = indexRow.getSmsRecord();

        populate(instance, indexRow);
        populate(instance, record);

        return instance;
    }

    private static void populate(
            final SmsIndexRowPayload instance,
            final SmsIndexRow indexRow
    ) {
        instance.setIndexRowId(indexRow.getId());
        instance.setActivityName(indexRow.getSmsActivity().getName());
        instance.setActivityId(indexRow.getSmsActivity().getId());
        instance.copyFrom(indexRow);
    }

    private static void populate(
            final SmsIndexRowPayload instance,
            final SmsRecord record
    ) {
        instance.setRecordId(record.getId());
        instance.copyFrom(record);
    }

    void copyFrom(SmsRecord record) {
        super.copyFrom(record);
        this.setSmsContent(record.getSmsContent());
    }
}
