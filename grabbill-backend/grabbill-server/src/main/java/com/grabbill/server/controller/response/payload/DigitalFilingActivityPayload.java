package com.grabbill.server.controller.response.payload;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingFile;
import com.grabbill.core.entity.DigitalFilingIndexRow;
import com.grabbill.core.entity.DigitalFilingRecord;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@Data
public class DigitalFilingActivityPayload extends BaseActivityPayload {

    private boolean sftp;
    private String sftpPath;

    public static DigitalFilingActivityPayload from(final DigitalFilingActivity digitalFilingActivity) {
        DigitalFilingActivityPayload instance = new DigitalFilingActivityPayload();
        instance.setId(digitalFilingActivity.getId());
        instance.setName(digitalFilingActivity.getName());
        instance.setPriority(digitalFilingActivity.getPriority());
        instance.setRetryCount(digitalFilingActivity.getRetryCount());
        instance.setMessage(digitalFilingActivity.getMessage());
        instance.setSftp(digitalFilingActivity.isSftp());
        instance.setSftpPath(digitalFilingActivity.getSftpPath());
        instance.setDraftTimestamp(digitalFilingActivity.getDraftTimestamp());
        instance.setSubmittedTimestamp(digitalFilingActivity.getSubmittedTimestamp());
        instance.setProcessingTimestamp(digitalFilingActivity.getProcessingTimestamp());
        instance.setProcessedTimestamp(digitalFilingActivity.getProcessedTimestamp());
        instance.setErrorTimestamp(digitalFilingActivity.getErrorTimestamp());
        instance.setCreatedBy(digitalFilingActivity.getCreatedBy());
        instance.setCreatedDate(digitalFilingActivity.getCreatedDate());
        instance.setLastModifiedBy(digitalFilingActivity.getLastModifiedBy());
        instance.setLastModifiedDate(digitalFilingActivity.getLastModifiedDate());
        instance.setExpectedPurgedTimestamp(digitalFilingActivity.getExpectedPurgedTimestamp());
        instance.setPurgedTimestamp(digitalFilingActivity.getPurgedTimestamp());
        instance.setPurgeBy(digitalFilingActivity.getPurgedBy());
        instance.setStatus(digitalFilingActivity.getStatus());

        List<BaseFilePayload> files = new ArrayList<>();
        for (DigitalFilingFile digitalFilingFile : digitalFilingActivity.getDigitalFilingFiles()) {
            files.add(toBaseFilePayload(digitalFilingFile));
        }
        instance.setFiles(files);

        List<BaseIndexRowPayload> indexRows = new ArrayList<>();
        for (DigitalFilingIndexRow indexRow : digitalFilingActivity.getDigitalFilingIndexRows()) {
            indexRows.add(toBaseIndexRowPayload(indexRow));
        }
        instance.setIndexRows(indexRows);

        List<BaseRecordPayload> records = new ArrayList<>();
        for (DigitalFilingRecord record : digitalFilingActivity.getDigitalFilingRecords()) {
            BaseRecordPayload recordPayload = new BaseRecordPayload();
            recordPayload.setId(record.getId());
            recordPayload.copyFrom(record);
            recordPayload.setIndexRow(toBaseIndexRowPayload(record.getDigitalFilingIndexRow()));
            records.add(recordPayload);
        }
        instance.setRecords(records);

        return instance;
    }

    private static BaseFilePayload toBaseFilePayload(final DigitalFilingFile digitalFilingFile) {
        BaseFilePayload filePayload = new BaseFilePayload();
        filePayload.setId(digitalFilingFile.getId());
        filePayload.copyFrom(digitalFilingFile);
        return filePayload;
    }

    private static BaseIndexRowPayload toBaseIndexRowPayload(final DigitalFilingIndexRow indexRow) {
        BaseIndexRowPayload indexRowPayload = new BaseIndexRowPayload();
        indexRowPayload.setId(indexRow.getId());
        indexRowPayload.copyFrom(indexRow);
        indexRowPayload.setFile(indexRow.getDigitalFilingFile() == null
                ? null : toBaseFilePayload(indexRow.getDigitalFilingFile()));
        return indexRowPayload;
    }

}
