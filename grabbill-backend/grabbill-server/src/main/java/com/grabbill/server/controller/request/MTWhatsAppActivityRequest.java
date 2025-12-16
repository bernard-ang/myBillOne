package com.grabbill.server.controller.request;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppIndexRow;
import com.grabbill.core.model.ProcessStatus;
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
public class MTWhatsAppActivityRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime scheduledTimestamp;

    @NotNull
    private ProcessStatus status;

    private boolean sftp;
    private String sftpPath;
    private List<BaseIndexRowRequest> indexRows = new ArrayList<>();


    public void to(final MTWhatsAppActivity activity) {
        activity.setName(this.name);
        activity.setStatus(this.status);
        activity.setSftp(this.sftp);
        activity.setSftpPath(this.sftpPath);

        if (scheduledTimestamp != null) {
            activity.setScheduledTimestamp(scheduledTimestamp);
        }

        List<MTWhatsAppIndexRow> targetIndexRows = activity.getMtWhatsAppIndexRows();
        int i = 0;
        for (; i < indexRows.size(); i++) {
            if (targetIndexRows.size() > i) {
                indexRows.get(i).to(targetIndexRows.get(i));

            } else {
                MTWhatsAppIndexRow indexRow = new MTWhatsAppIndexRow();
                indexRow.setMtWhatsAppActivity(activity);
                indexRow.setMtWhatsAppType(activity.getMtWhatsAppType());
                indexRows.get(i).to(indexRow);
                activity.getMtWhatsAppIndexRows().add(indexRow);
            }
        }

        int currentSize = targetIndexRows.size();
        int newSize = indexRows.size();
        for(; i < currentSize; i++) {
            activity.getMtWhatsAppIndexRows().remove(newSize);
        }
    }

}
