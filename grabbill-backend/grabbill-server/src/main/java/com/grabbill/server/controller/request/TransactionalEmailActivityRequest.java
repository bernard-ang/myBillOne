package com.grabbill.server.controller.request;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailIndexRow;
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
public class TransactionalEmailActivityRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String emailFrom;

    @Size(max = 255)
    private String emailFromName;

    @Size(max = 255)
    private String emailSubject;

    private Boolean sendWhatsAppMessage;

    private String emailContent;
    private String emailMjmlContent;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime scheduledTimestamp;

    @NotNull
    private ProcessStatus status;

    private List<BaseIndexRowRequest> indexRows = new ArrayList<>();


    public void to(final TransactionalEmailActivity transactionalEmailActivity) {
        transactionalEmailActivity.setName(this.name);
        transactionalEmailActivity.setEmailFrom(this.emailFrom);
        transactionalEmailActivity.setEmailFromName(this.emailFromName);
        transactionalEmailActivity.setEmailSubject(this.emailSubject);
        transactionalEmailActivity.setEmailContent(this.emailContent);
        transactionalEmailActivity.setEmailMjmlContent(this.emailMjmlContent);
        transactionalEmailActivity.setStatus(this.status);
        transactionalEmailActivity.setSendWhatsAppMessage(this.sendWhatsAppMessage);

        if (scheduledTimestamp != null) {
            transactionalEmailActivity.setScheduledTimestamp(scheduledTimestamp);
        }

        List<TransactionalEmailIndexRow> targetDigitalFilingIndexRows = transactionalEmailActivity.getTransactionalEmailIndexRows();
        int i = 0;
        for (; i < indexRows.size(); i++) {
            if (targetDigitalFilingIndexRows.size() > i) {
                indexRows.get(i).to(targetDigitalFilingIndexRows.get(i));

            } else {
                TransactionalEmailIndexRow indexRow = new TransactionalEmailIndexRow();
                indexRow.setTransactionalEmailActivity(transactionalEmailActivity);
                indexRow.setTransactionalEmailType(transactionalEmailActivity.getTransactionalEmailType());
                indexRows.get(i).to(indexRow);
                transactionalEmailActivity.getTransactionalEmailIndexRows().add(indexRow);
            }
        }

        int currentSize = targetDigitalFilingIndexRows.size();
        int newSize = indexRows.size();
        for(; i < currentSize; i++) {
            transactionalEmailActivity.getTransactionalEmailIndexRows().remove(newSize);
        }
    }

}
