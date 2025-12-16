package com.grabbill.server.controller.response.payload;

import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class SmsActivityCreditUsagePayload implements ApiPayload {

    private List<IndexRowUsage> indexRowUsages = new ArrayList<>();
    private int totalSms;
    private int estimatedCreditUsage;


    public static SmsActivityCreditUsagePayload from(
            final List<IndexRowUsage> indexRowUsages,
            final int totalSms,
            final int estimatedCreditUsage
    ) {
        SmsActivityCreditUsagePayload payload = new SmsActivityCreditUsagePayload();
        payload.getIndexRowUsages().addAll(indexRowUsages);
        payload.setTotalSms(totalSms);
        payload.setEstimatedCreditUsage(estimatedCreditUsage);

        return payload;
    }

    @Data
    public static class IndexRowUsage {

        private String email;
        private String mobileNo;
        private String smsContent;
        private int totalBytes;
        private int totalCredits;
        private boolean validMobileNo;

    }

}
