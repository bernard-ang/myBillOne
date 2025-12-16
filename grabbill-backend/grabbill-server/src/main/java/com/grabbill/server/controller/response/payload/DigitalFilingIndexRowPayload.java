package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.DigitalFilingFile;
import com.grabbill.core.entity.DigitalFilingIndexRow;
import com.grabbill.core.entity.DigitalFilingRecord;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class DigitalFilingIndexRowPayload extends BaseIndexRowFullPayload {

    public static DigitalFilingIndexRowPayload from(final DigitalFilingIndexRow indexRow) {
        DigitalFilingIndexRowPayload instance = new DigitalFilingIndexRowPayload();
        DigitalFilingFile file = indexRow.getDigitalFilingFile();
        DigitalFilingRecord record = indexRow.getDigitalFilingRecord();

        populate(instance, indexRow);
        populate(instance, file);
        populate(instance, record);

        return instance;
    }

    private static void populate(
            final DigitalFilingIndexRowPayload instance,
            final DigitalFilingIndexRow indexRow
    ) {
        instance.setIndexRowId(indexRow.getId());
        instance.setActivityName(indexRow.getDigitalFilingActivity().getName());
        instance.setActivityId(indexRow.getDigitalFilingActivity().getId());
        instance.copyFrom(indexRow);
    }

    private static void populate(
            final DigitalFilingIndexRowPayload instance,
            final DigitalFilingFile file
    ) {
        if(file != null) {
            instance.copyFrom(file.getId(), file);
        }
    }

    private static void populate(
            final DigitalFilingIndexRowPayload instance,
            final DigitalFilingRecord record
    ) {
        instance.setRecordId(record.getId());
        instance.copyFrom(record);
    }

}
