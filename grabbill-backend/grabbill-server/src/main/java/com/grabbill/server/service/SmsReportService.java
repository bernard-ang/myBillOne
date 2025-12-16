package com.grabbill.server.service;

import com.grabbill.core.entity.SmsActivity;
import com.grabbill.core.model.ReportType;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author michaellow
 */
public interface SmsReportService {

    Workbook generateSmsReport(List<SmsActivity> activities, List<ReportType> reportTypes, ZoneId zoneId);

}
