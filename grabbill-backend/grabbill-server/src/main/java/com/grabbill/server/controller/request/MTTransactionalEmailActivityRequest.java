package com.grabbill.server.controller.request;

import com.grabbill.core.entity.*;
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
public class MTTransactionalEmailActivityRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private boolean sftp;

    private String sftpPath;

    @Size(max = 255)
    private String emailFrom;

    @Size(max = 255)
    private String emailFromName;

    private Boolean sendWhatsAppMessage;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime scheduledTimestamp;

    @NotNull
    private ProcessStatus status;

    private List<BaseIndexRowRequest> indexRows = new ArrayList<>();

    private List<MTTransactionalEmailActivityTemplateRequest> transactionalEmailActivityTemplates = new ArrayList<>();


    public void to(final MTTransactionalEmailActivity mtTransactionalEmailActivity) {
        mtTransactionalEmailActivity.setName(this.name);
        mtTransactionalEmailActivity.setSftp(this.sftp);
        mtTransactionalEmailActivity.setSftpPath(this.sftpPath);
        mtTransactionalEmailActivity.setEmailFrom(this.emailFrom);
        mtTransactionalEmailActivity.setEmailFromName(this.emailFromName);
        mtTransactionalEmailActivity.setStatus(this.status);
        mtTransactionalEmailActivity.setSendWhatsAppMessage(this.sendWhatsAppMessage);

        if (scheduledTimestamp != null) {
            mtTransactionalEmailActivity.setScheduledTimestamp(scheduledTimestamp);
        }

        List<MTTransactionalEmailActivityTemplate> targetTemplates = mtTransactionalEmailActivity.getMtTransactionalEmailActivityTemplates();
        int i = 0;
        for (; i < transactionalEmailActivityTemplates.size(); i++) {
            if (targetTemplates.size() > i) {
                transactionalEmailActivityTemplates.get(i).to(targetTemplates.get(i));

            } else {
                MTTransactionalEmailActivityTemplate emailTemplate = new MTTransactionalEmailActivityTemplate();
                emailTemplate.setMtTransactionalEmailActivity(mtTransactionalEmailActivity);
                transactionalEmailActivityTemplates.get(i).to(emailTemplate);
                mtTransactionalEmailActivity.getMtTransactionalEmailActivityTemplates().add(emailTemplate);
            }
        }

        int currentSize = targetTemplates.size();
        int newSize = transactionalEmailActivityTemplates.size();
        for(; i < currentSize; i++) {
            mtTransactionalEmailActivity.getMtTransactionalEmailActivityTemplates().remove(newSize);
        }


        List<MTTransactionalEmailIndexRow> targetDigitalFilingIndexRows = mtTransactionalEmailActivity.getMtTransactionalEmailIndexRows();
        int j = 0;
        for (; j < indexRows.size(); j++) {
            if (targetDigitalFilingIndexRows.size() > j) {
                indexRows.get(j).to(targetDigitalFilingIndexRows.get(j));

            } else {
                MTTransactionalEmailIndexRow indexRow = new MTTransactionalEmailIndexRow();
                indexRow.setMtTransactionalEmailActivity(mtTransactionalEmailActivity);
                indexRow.setMtTransactionalEmailType(mtTransactionalEmailActivity.getMtTransactionalEmailType());
                indexRows.get(j).to(indexRow);
                mtTransactionalEmailActivity.getMtTransactionalEmailIndexRows().add(indexRow);
            }
        }

        int currentIndexRowsSize = targetDigitalFilingIndexRows.size();
        int newIndexRowsSize = indexRows.size();
        for(; j < currentIndexRowsSize; j++) {
            mtTransactionalEmailActivity.getMtTransactionalEmailIndexRows().remove(newIndexRowsSize);
        }


    }

}
