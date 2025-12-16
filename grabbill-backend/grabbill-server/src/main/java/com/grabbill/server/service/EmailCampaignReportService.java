package com.grabbill.server.service;

import com.grabbill.core.entity.EmailCampaignActivity;
import com.grabbill.core.model.ReportType;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author michaellow
 */
public interface EmailCampaignReportService {

    Workbook generateEmailCampaignReport(List<EmailCampaignActivity> activities, List<ReportType> reportTypes, ZoneId zoneId);

}
