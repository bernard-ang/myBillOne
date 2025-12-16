package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class DigitalFilingTypeBasicPayload extends BaseTypeBasicPayload {

    private String lastUploadBy;

    private OffsetDateTime lastUploadDate;


    public static DigitalFilingTypeBasicPayload from (
            final DigitalFilingType digitalFilingType,
            final List<DigitalFilingActivity> activities
    ) {
        DigitalFilingTypeBasicPayload instance = new DigitalFilingTypeBasicPayload();
        instance.setId(digitalFilingType.getId());
        instance.setLastUploadBy(digitalFilingType.getLastUploadBy());
        instance.setLastUploadDate(digitalFilingType.getLastUploadDate());
        instance.copyFrom(digitalFilingType);

        int noOfFiles = 0;
        for (DigitalFilingActivity activity : activities) {
            noOfFiles += activity.getDigitalFilingFiles().size();
        }
        instance.setNoOfFiles(noOfFiles);

        return instance;
    }

}
