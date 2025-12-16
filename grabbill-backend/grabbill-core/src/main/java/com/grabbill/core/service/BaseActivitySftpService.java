package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.sftp.SftpFolderValidationSummary;

import java.util.List;

/**
 * @author michaellow
 */
public interface BaseActivitySftpService<T extends BaseType, A extends BaseActivity, IF extends BaseIndexField> {

    String IN_FOLDER = "in";
    String ARCHIVE_FOLDER = "arc";
    String ERROR_FOLDER = "err";
    String PDF_EXT = ".pdf";
    String EXCEL_EXT = ".xlsx";
    String CSV_EXT = ".csv";


    SftpFolderValidationSummary validate(Account account, T type, List<IF> indexFields, A activity) throws GrabbillException;

    void process(Account account, T type, List<IF> indexFields, A activity) throws GrabbillException;

}
