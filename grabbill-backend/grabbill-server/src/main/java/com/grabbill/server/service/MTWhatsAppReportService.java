package com.grabbill.server.service;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.model.ReportType;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author seez
 */
public interface MTWhatsAppReportService {

    Workbook generateWhatsAppReport(List<MTWhatsAppActivity> activities, List<ReportType> reportTypes, ZoneId zoneId, Boolean encryptAttachmentPassword);

}
