package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.SmsActivity;
import lombok.Data;

/**
 * @author michaellow
 */
@Data
public class SmsActivityBasicPayload extends BaseActivityBasicPayload {

    public static SmsActivityBasicPayload from(
            final SmsActivity smsActivity
    ) {
        SmsActivityBasicPayload instance = new SmsActivityBasicPayload();
        instance.setId(smsActivity.getId());
        instance.setNoOfFiles(0);
        instance.copyFrom(smsActivity);

        return instance;
    }

}
