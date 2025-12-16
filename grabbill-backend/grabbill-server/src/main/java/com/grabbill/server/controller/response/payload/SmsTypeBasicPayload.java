package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.SmsType;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Data
public class SmsTypeBasicPayload extends BaseTypeBasicPayload {

    private String lastSentBy;

    private OffsetDateTime lastSentDate;


    public static SmsTypeBasicPayload from (
            final SmsType type
    ) {
        SmsTypeBasicPayload instance = new SmsTypeBasicPayload();
        instance.setId(type.getId());
        instance.setLastSentBy(type.getLastSentBy());
        instance.setLastSentDate(type.getLastSentDate());
        instance.copyFrom(type);

        return instance;
    }

}
