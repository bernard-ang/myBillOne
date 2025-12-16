package com.grabbill.server.service;

import com.grabbill.core.entity.MTTransactionalEmailActivity;
import com.grabbill.core.model.ReportType;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author michaellow
 */
public interface MTTransactionalEmailReportService {

    Workbook generateMtTransactionalEmailReport(List<MTTransactionalEmailActivity> activities, List<ReportType> reportTypes, ZoneId zoneId, Boolean encryptAttachmentPassword, boolean hasWhatsApp);

}
