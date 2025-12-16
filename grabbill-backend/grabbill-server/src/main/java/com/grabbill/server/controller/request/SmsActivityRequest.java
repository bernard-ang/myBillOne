package com.grabbill.server.controller.request;

import com.grabbill.core.entity.SmsActivity;
import com.grabbill.core.entity.SmsIndexRow;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.utils.SmsUtils;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class SmsActivityRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String smsFrom;

    @Size(max = 255)
    private String smsContent;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime scheduledTimestamp;

    @NotNull
    private ProcessStatus status;

    private Integer contactGroupId;
    private List<BaseIndexRowRequest> indexRows = new ArrayList<>();


    public void to(final SmsActivity smsActivity) {
        smsActivity.setName(this.name);
        smsActivity.setSmsFrom(this.smsFrom);
        smsActivity.setSmsContent(SmsUtils.appendRM0(this.smsContent));
        smsActivity.setStatus(this.status);

        if (scheduledTimestamp != null) {
            smsActivity.setScheduledTimestamp(scheduledTimestamp);
        }

        List<SmsIndexRow> targetIndexRows = smsActivity.getSmsIndexRows();
        int i = 0;
        for (; i < indexRows.size(); i++) {
            if (targetIndexRows.size() > i) {
                indexRows.get(i).to(targetIndexRows.get(i));

            } else {
                SmsIndexRow indexRow = new SmsIndexRow();
                indexRow.setSmsActivity(smsActivity);
                indexRow.setSmsType(smsActivity.getSmsType());
                indexRows.get(i).to(indexRow);
                smsActivity.getSmsIndexRows().add(indexRow);
            }
        }

        int currentSize = targetIndexRows.size();
        int newSize = indexRows.size();
        for(; i < currentSize; i++) {
            smsActivity.getSmsIndexRows().remove(newSize);
        }

        for (SmsIndexRow smsIndexRow : smsActivity.getSmsIndexRows()) {
            String phoneNumber = smsIndexRow.getText1();
            if (SmsUtils.isValidPhoneNumber(phoneNumber) && !phoneNumber.startsWith("6")) {
                smsIndexRow.setText1("6" + phoneNumber);
            }
        }
    }

}
