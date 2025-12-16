package com.grabbill.server.service;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.model.ReportType;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author michaellow
 */
public interface TransactionalEmailReportService {

    Workbook generateTransactionalEmailReport(List<TransactionalEmailActivity> activities, List<ReportType> reportTypes, ZoneId zoneId, Boolean encryptAttachmentPassword, boolean hasWhatsApp);

}
