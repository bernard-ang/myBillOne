package com.grabbill.server.controller.request;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingIndexRow;
import com.grabbill.core.model.ProcessStatus;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class DigitalFilingActivityRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private ProcessStatus status;

    private boolean sftp;
    private String sftpPath;
    private List<BaseIndexRowRequest> indexRows = new ArrayList<>();


    public void to(final DigitalFilingActivity digitalFilingActivity) {
        digitalFilingActivity.setName(this.name);
        digitalFilingActivity.setStatus(this.status);
        digitalFilingActivity.setSftp(this.sftp);
        digitalFilingActivity.setSftpPath(this.sftpPath);

        List<DigitalFilingIndexRow> targetDigitalFilingIndexRows = digitalFilingActivity.getDigitalFilingIndexRows();
        int i = 0;
        for (; i < indexRows.size(); i++) {
            if (targetDigitalFilingIndexRows.size() > i) {
                indexRows.get(i).to(targetDigitalFilingIndexRows.get(i));

            } else {
                DigitalFilingIndexRow indexRow = new DigitalFilingIndexRow();
                indexRow.setDigitalFilingActivity(digitalFilingActivity);
                indexRow.setDigitalFilingType(digitalFilingActivity.getDigitalFilingType());
                indexRows.get(i).to(indexRow);
                digitalFilingActivity.getDigitalFilingIndexRows().add(indexRow);
            }
        }

        int currentSize = targetDigitalFilingIndexRows.size();
        int newSize = indexRows.size();
        for(; i < currentSize; i++) {
            digitalFilingActivity.getDigitalFilingIndexRows().remove(newSize);
        }
    }

}
