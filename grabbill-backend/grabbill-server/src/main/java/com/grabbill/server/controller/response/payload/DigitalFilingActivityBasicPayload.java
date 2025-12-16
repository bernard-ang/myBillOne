package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.DigitalFilingActivity;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class DigitalFilingActivityBasicPayload extends BaseActivityBasicPayload {

    public static DigitalFilingActivityBasicPayload from(final DigitalFilingActivity digitalFilingActivity) {
        DigitalFilingActivityBasicPayload instance = new DigitalFilingActivityBasicPayload();
        instance.setId(digitalFilingActivity.getId());
        instance.setNoOfFiles(digitalFilingActivity.getDigitalFilingFiles().size());
        instance.copyFrom(digitalFilingActivity);

        return instance;
    }

}
