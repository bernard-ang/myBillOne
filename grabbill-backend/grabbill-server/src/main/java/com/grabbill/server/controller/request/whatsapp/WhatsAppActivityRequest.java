package com.grabbill.server.controller.request.whatsapp;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppIndexRow;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.server.controller.request.BaseIndexRowRequest;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author seez
 */
@Data
public class WhatsAppActivityRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime scheduledTimestamp;

    @NotNull
    private ProcessStatus status;

    private List<BaseIndexRowRequest> indexRows = new ArrayList<>();


    public void to(final WhatsAppActivity activity) {
        activity.setName(this.name);
        activity.setStatus(this.status);

        if (scheduledTimestamp != null) {
            activity.setScheduledTimestamp(scheduledTimestamp);
        }

        List<WhatsAppIndexRow> targetIndexRows = activity.getWhatsAppIndexRows();
        int i = 0;
        for (; i < indexRows.size(); i++) {
            if (targetIndexRows.size() > i) {
                indexRows.get(i).to(targetIndexRows.get(i));

            } else {
                WhatsAppIndexRow indexRow = new WhatsAppIndexRow();
                indexRow.setWhatsAppActivity(activity);
                indexRow.setWhatsAppType(activity.getWhatsAppType());
                indexRows.get(i).to(indexRow);
                activity.getWhatsAppIndexRows().add(indexRow);
            }
        }

        int currentSize = targetIndexRows.size();
        int newSize = indexRows.size();
        for(; i < currentSize; i++) {
            activity.getWhatsAppIndexRows().remove(newSize);
        }
    }

}
