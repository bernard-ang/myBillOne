package com.grabbill.server.service;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.model.ReportType;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author michaellow
 */
public interface DigitalFilingReportService {

    Workbook generateDigitalFilingReport(List<DigitalFilingActivity> activities, List<ReportType> reportTypes, ZoneId zoneId);

}
