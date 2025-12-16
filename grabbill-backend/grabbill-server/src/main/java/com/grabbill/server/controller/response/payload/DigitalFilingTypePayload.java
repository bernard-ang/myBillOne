package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingIndexField;
import com.grabbill.core.entity.DigitalFilingType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class DigitalFilingTypePayload extends BaseTypePayload {

    private String csvSeparator;

    boolean autoPurge;
    Integer autoPurgeByDays;

    public static DigitalFilingTypePayload from(
            final DigitalFilingType digitalFilingType,
            final List<DigitalFilingActivity> digitalFilingActivities
    ) {
        DigitalFilingTypePayload instance = new DigitalFilingTypePayload();
        instance.setId(digitalFilingType.getId());
        instance.setCsvSeparator(digitalFilingType.getCsvSeparator());
        instance.copyFrom(digitalFilingType);
        instance.setAutoPurge(digitalFilingType.isAutoPurge());
        instance.setAutoPurgeByDays(digitalFilingType.getAutoPurgeByDays());

        if (digitalFilingActivities != null && !digitalFilingActivities.isEmpty()) {
            int noOfFiles = 0;
            for (DigitalFilingActivity digitalFilingActivity : digitalFilingActivities) {
                noOfFiles += digitalFilingActivity.getDigitalFilingFiles().size();
            }
            instance.setNoOfFiles(noOfFiles);
        }

        List<BaseIndexFieldPayload> indexFields = new ArrayList<>();
        for (DigitalFilingIndexField digitalFilingIndexField : digitalFilingType.getDigitalFilingIndexFields()) {
            BaseIndexFieldPayload indexFieldPayload = new BaseIndexFieldPayload();
            indexFieldPayload.setId(digitalFilingIndexField.getId());
            indexFieldPayload.copyFrom(digitalFilingIndexField);
            indexFields.add(indexFieldPayload);
        }
        instance.setIndexFields(indexFields);

        return instance;
    }

}
