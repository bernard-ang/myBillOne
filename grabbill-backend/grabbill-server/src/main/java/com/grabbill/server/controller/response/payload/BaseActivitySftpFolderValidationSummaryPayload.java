package com.grabbill.server.controller.response.payload;

import com.grabbill.core.model.sftp.SftpFolderValidationSummary;
import com.grabbill.server.controller.response.ApiPayload;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author michaellow
 */
@Data
public class BaseActivitySftpFolderValidationSummaryPayload implements ApiPayload {

    private boolean hasError;
    private Map<Integer, List<IndexRowError>> indexRowErrors = new HashMap<>();
    private Map<String, String> fileErrors = new HashMap<>();
    private List<String> genericErrors = new ArrayList<>();


    @Data
    public static class IndexRowError {

        private int columnNo;
        private String error;


        public static IndexRowError from(final SftpFolderValidationSummary.IndexRowError from) {
            IndexRowError to = new IndexRowError();
            to.columnNo = from.getColumnNo();
            to.error = from.getError();

            return to;
        }

    }

    public static BaseActivitySftpFolderValidationSummaryPayload ok() {
        BaseActivitySftpFolderValidationSummaryPayload payload =
                new BaseActivitySftpFolderValidationSummaryPayload();
        payload.hasError = false;
        return payload;
    }

    public static BaseActivitySftpFolderValidationSummaryPayload from(
            final SftpFolderValidationSummary summary
    ) {
        BaseActivitySftpFolderValidationSummaryPayload payload =
                new BaseActivitySftpFolderValidationSummaryPayload();

        payload.hasError = summary.hasError();
        if (summary.hasError()) {
            for (Integer rowNo : summary.getIndexRowErrors().keySet()) {

                List<IndexRowError> errors = new ArrayList<>();
                for (SftpFolderValidationSummary.IndexRowError indexRowError : summary.getIndexRowErrors().get(rowNo)) {
                    errors.add(IndexRowError.from(indexRowError));
                }

                payload.indexRowErrors.put(rowNo, errors);
            }

            payload.fileErrors.putAll(summary.getFileErrors());
            payload.genericErrors.addAll(summary.getGenericErrors());
        }

        return payload;
    }

}
