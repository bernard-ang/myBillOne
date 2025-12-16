package com.grabbill.server.service;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.model.ReportType;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author seez
 */
public interface WhatsAppReportService {

    Workbook generateWhatsAppReport(List<WhatsAppActivity> activities, List<ReportType> reportTypes, ZoneId zoneId, Boolean encryptAttachmentPassword);

}
