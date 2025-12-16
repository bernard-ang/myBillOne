package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ReportType;
import com.grabbill.server.dto.report.ActivityRecordRow;
import com.grabbill.server.dto.report.ActivitySummaryRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author seez
 */
public class MTWhatsAppReportServiceImpl extends BaseReportService implements MTWhatsAppReportService {

    @Override
    public Workbook generateWhatsAppReport(
            final List<MTWhatsAppActivity> activities,
            final List<ReportType> reportTypes,
            final ZoneId zoneId,
            final Boolean encryptAttachmentPassword
    ) {
        Workbook workbook = new XSSFWorkbook();
        for (ReportType reportType : reportTypes) {
            if (ReportType.ACTIVITY_SUMMARY.equals(reportType)) {
                List<ActivitySummaryRow> activitySummaryRows = new ArrayList<>();
                for (MTWhatsAppActivity activity : activities) {
                    MTWhatsAppType type = activity.getMtWhatsAppType();

                    ActivitySummaryRow activitySummaryRow = new ActivitySummaryRow();
                    activitySummaryRow.setTypeId(type.getId());
                    activitySummaryRow.setTypeName(type.getName());
                    activitySummaryRow.setActivityId(activity.getId());
                    activitySummaryRow.setActivityName(activity.getName());
                    if (activity.getSubmittedTimestamp() != null) {
                        activitySummaryRow.setStartAt(ZonedDateTime.ofInstant(activity.getSubmittedTimestamp().toInstant(), zoneId));
                    }
                    if (activity.getProcessedTimestamp() != null) {
                        activitySummaryRow.setCompletedAt(ZonedDateTime.ofInstant(activity.getProcessedTimestamp().toInstant(), zoneId));
                    }

                    activitySummaryRow.setTotalWhatsApp(activity.getMtWhatsAppIndexRows().size());
                    activitySummaryRow.setTotalWhatsAppSent(activity.getWhatsAppStatusSent());
                    activitySummaryRow.setTotalWhatsAppSkip(activity.getWhatsAppStatusSkip());
                    activitySummaryRow.setTotalWhatsAppRead(activity.getWhatsAppStatusRead());
                    activitySummaryRow.setTotalWhatsAppDelivered(activity.getWhatsAppStatusDelivered());
                    activitySummaryRow.setTotalWhatsAppAcknowledge(activity.getWhatsAppStatusAcknowledge());
                    activitySummaryRow.setTotalWhatsAppFailed(activity.getWhatsAppStatusFailed());

                    activitySummaryRows.add(activitySummaryRow);
                }
                buildActivitySummarySheet(workbook, activitySummaryRows, true);

            } else if (ReportType.ACTIVITY_RECORDS.equals(reportType)) {
                List<List<ActivityRecordRow>> allRecordRows = new ArrayList<>();
                Set<String> dynamicHeaders = new LinkedHashSet<>();
                for (MTWhatsAppActivity activity : activities) {
                    MTWhatsAppType type = activity.getMtWhatsAppType();
                    List<MTWhatsAppIndexField> indexFields = type.getMtWhatsAppIndexFields();

                    List<ActivityRecordRow> activityRecordRows = new ArrayList<>();
                    for (MTWhatsAppRecord record : activity.getMtWhatsAppRecords()) {
                        MTWhatsAppIndexRow indexRow = record.getMtWhatsAppIndexRow();

                        ActivityRecordRow activityRecordRow = new ActivityRecordRow();
                        activityRecordRow.setRecordId(record.getId());
                        activityRecordRow.setTypeId(type.getId());
                        activityRecordRow.setTypeName(type.getName());
                        activityRecordRow.setActivityId(activity.getId());
                        activityRecordRow.setActivityName(activity.getName());
                        activityRecordRow.setMobileNo(record.getName());
                        activityRecordRow.setProcessStatus(record.getStatus());
                        activityRecordRow.setMessage(record.getMessage());

                        activityRecordRow.setWhatsAppTemplateBody(record.getWhatsAppBodyContent());
                        activityRecordRow.setWhatsAppStatusSent(record.getWhatsAppStatusSent() != null && record.getWhatsAppStatusSent());
                        activityRecordRow.setWhatsAppStatusSkip(record.getWhatsAppStatusSkip() != null && record.getWhatsAppStatusSkip());
                        activityRecordRow.setWhatsAppStatusDelivered(record.getWhatsAppStatusDelivered() != null && record.getWhatsAppStatusDelivered());
                        activityRecordRow.setWhatsAppStatusRead(record.getWhatsAppStatusRead() != null && record.getWhatsAppStatusRead());
                        activityRecordRow.setWhatsAppStatusAcknowledge(record.getWhatsAppStatusAcknowledge() != null && record.getWhatsAppStatusAcknowledge());
                        activityRecordRow.setWhatsAppStatusFailed(record.getWhatsAppStatusFailed() != null && record.getWhatsAppStatusFailed());
                        activityRecordRow.setWhatsAppStatusFailedMessage(record.getWhatsAppStatusFailedMessage());

                        for (int i = 0; i < indexFields.size(); i++) {
                            MTWhatsAppIndexField indexField = indexFields.get(i);
                            if (indexField.isApplicable()) {
                                if (indexField.getLabel().equals("Attachment Password")) {
                                    activityRecordRow.getDynamicFields().put(indexField.getLabel(), encryptAttachmentPassword ? "***" : getValue(i, indexRow, indexField.getDataType()));
                                    dynamicHeaders.add(indexField.getLabel());
                                } else {
                                    activityRecordRow.getDynamicFields().put(indexField.getLabel(), getValue(i, indexRow, indexField.getDataType()));
                                    dynamicHeaders.add(indexField.getLabel());
                                }
                            }
                        }

                        activityRecordRows.add(activityRecordRow);
                    }
                    allRecordRows.add(activityRecordRows);
                }

                buildActivityRecordsSheet(workbook, dynamicHeaders, allRecordRows, true);

            }
        }

        return workbook;
    }


    protected void buildActivitySummarySheet(
            final Workbook workbook,
            final List<ActivitySummaryRow> activitySummaryRows,
            final boolean hasWhatsApp
    ) {
        Sheet sheet = workbook.createSheet("activity summary");
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 6000);
        sheet.setColumnWidth(5, 6000);
        sheet.setColumnWidth(6, 4000);
        sheet.setColumnWidth(7, 4000);
        sheet.setColumnWidth(8, 4000);
        sheet.setColumnWidth(9, 4000);
        sheet.setColumnWidth(10, 4000);
        sheet.setColumnWidth(11, 4000);
        sheet.setColumnWidth(12, 4000);

        CellStyle headerCellStyle = createHeaderCellStyle(workbook);

        int rowIndex = 0;
        Row hearderRow = sheet.createRow(rowIndex);
        Cell headerCell = hearderRow.createCell(0);
        headerCell.setCellValue("Type Id");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(1);
        headerCell.setCellValue("Type Name");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(2);
        headerCell.setCellValue("Activity Id");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(3);
        headerCell.setCellValue("Activity Name");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(4);
        headerCell.setCellValue("Start At");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(5);
        headerCell.setCellValue("Completed At");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(6);
        headerCell.setCellValue("Total");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(7);
        headerCell.setCellValue("Total Sent");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(8);
        headerCell.setCellValue("Total Skip");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(9);
        headerCell.setCellValue("Total Delivered");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(10);
        headerCell.setCellValue("Total Read");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(11);
        headerCell.setCellValue("Total Acknowledged");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(12);
        headerCell.setCellValue("Total Failed");
        headerCell.setCellStyle(headerCellStyle);

        int total = 0;
        int totalSent = 0;
        int totalSkip = 0;
        int totalRead = 0;
        int totalDelivered = 0;
        int totalAcknolwedge = 0;
        int totalFailed = 0;

        for (ActivitySummaryRow activitySummaryRow : activitySummaryRows) {

            Row row = sheet.createRow(++rowIndex);

            Cell cell = row.createCell(0);
            cell.setCellValue(activitySummaryRow.getTypeId());

            cell = row.createCell(1);
            cell.setCellValue(activitySummaryRow.getTypeName());

            cell = row.createCell(2);
            cell.setCellValue(activitySummaryRow.getActivityId());

            cell = row.createCell(3);
            cell.setCellValue(activitySummaryRow.getActivityName());

            cell = row.createCell(4);
            if (activitySummaryRow.getStartAt() != null) {
                cell.setCellValue(DATE_TIME_FORMATTER.format(activitySummaryRow.getStartAt()));
            }

            cell = row.createCell(5);
            if (activitySummaryRow.getCompletedAt() != null) {
                cell.setCellValue(DATE_TIME_FORMATTER.format(activitySummaryRow.getCompletedAt()));
            }

            cell = row.createCell(6);
            cell.setCellValue(activitySummaryRow.getTotalWhatsApp());
            total += activitySummaryRow.getTotalWhatsApp();

            cell = row.createCell(7);
            cell.setCellValue(activitySummaryRow.getTotalWhatsAppSent());
            totalSent += activitySummaryRow.getTotalWhatsAppSent();

            cell = row.createCell(8);
            cell.setCellValue(activitySummaryRow.getTotalWhatsAppSkip());
            totalSkip += activitySummaryRow.getTotalWhatsAppSkip();

            cell = row.createCell(9);
            cell.setCellValue(activitySummaryRow.getTotalWhatsAppDelivered());
            totalDelivered += activitySummaryRow.getTotalWhatsAppDelivered();

            cell = row.createCell(10);
            cell.setCellValue(activitySummaryRow.getTotalWhatsAppRead());
            totalRead += activitySummaryRow.getTotalWhatsAppRead();

            cell = row.createCell(11);
            cell.setCellValue(activitySummaryRow.getTotalWhatsAppAcknowledge());
            totalAcknolwedge += activitySummaryRow.getTotalWhatsAppAcknowledge();

            cell = row.createCell(12);
            cell.setCellValue(activitySummaryRow.getTotalWhatsAppFailed());
            totalFailed += activitySummaryRow.getTotalWhatsAppFailed();
        }

        rowIndex = rowIndex + 2;
        Row row = sheet.createRow(rowIndex);
        CellStyle summaryCellStyle = createSummaryCellStyle(workbook);

        Cell cell = row.createCell(5);
        cell.setCellValue("Total");
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(6);
        cell.setCellValue(total);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(7);
        cell.setCellValue(totalSent);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(8);
        cell.setCellValue(totalSkip);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(9);
        cell.setCellValue(totalDelivered);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(10);
        cell.setCellValue(totalRead);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(11);
        cell.setCellValue(totalAcknolwedge);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(12);
        cell.setCellValue(totalFailed);
        cell.setCellStyle(summaryCellStyle);

    }

    protected void buildActivityRecordsSheet(
            final Workbook workbook,
            final Set<String> dynamicHeaders,
            final List<List<ActivityRecordRow>> allRecordRows,
            final boolean hasWhatsApp
    ) {
        Sheet sheet = workbook.createSheet("activity records");
        CellStyle headerCellStyle = createHeaderCellStyle(workbook);
        Map<String, Integer> dynamicHeaderToIndexMap = new HashMap<>();
        int dynamicHeaderIndex = 0;
        for (String dynamicHeader : dynamicHeaders) {
            dynamicHeaderToIndexMap.put(dynamicHeader, dynamicHeaderIndex++);
        }

        int rowIndex = 0;
        Row hearderRow = sheet.createRow(rowIndex > 0 ? ++rowIndex : rowIndex);
        sheet.setColumnWidth(0, 4000);
        Cell headerCell = hearderRow.createCell(0);
        headerCell.setCellValue("Type Id");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(1, 4000);
        headerCell = hearderRow.createCell(1);
        headerCell.setCellValue("Type Name");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(2, 4000);
        headerCell = hearderRow.createCell(2);
        headerCell.setCellValue("Activity Id");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(3, 4000);
        headerCell = hearderRow.createCell(3);
        headerCell.setCellValue("Activity Name");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(4, 4000);
        headerCell = hearderRow.createCell(4);
        headerCell.setCellValue("Record Id");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(5, 4000);
        headerCell = hearderRow.createCell(5);
        headerCell.setCellValue("Status");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(6, 4000);
        headerCell = hearderRow.createCell(6);
        headerCell.setCellValue("Message");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(7, 4000);
        headerCell = hearderRow.createCell(7);
        headerCell.setCellValue("Body Content");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(8, 4000);
        headerCell = hearderRow.createCell(8);
        headerCell.setCellValue("Sent");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(9, 4000);
        headerCell = hearderRow.createCell(9);
        headerCell.setCellValue("Skipped");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(10, 4000);
        headerCell = hearderRow.createCell(10);
        headerCell.setCellValue("Delivered");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(11, 4000);
        headerCell = hearderRow.createCell(11);
        headerCell.setCellValue("Read");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(12, 4000);
        headerCell = hearderRow.createCell(12);
        headerCell.setCellValue("Acknowledged");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(13, 4000);
        headerCell = hearderRow.createCell(13);
        headerCell.setCellValue("Failed");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(14, 4000);
        headerCell = hearderRow.createCell(14);
        headerCell.setCellValue("Failed Message");
        headerCell.setCellStyle(headerCellStyle);

        if (!dynamicHeaders.isEmpty()) {
            int columnIndex = 15;
            for (String dynamicHeader : dynamicHeaders) {
                sheet.setColumnWidth(columnIndex, 4000);
                headerCell = hearderRow.createCell(columnIndex++);
                headerCell.setCellValue(dynamicHeader + "*");
                headerCell.setCellStyle(headerCellStyle);
            }
        }

        for (List<ActivityRecordRow> activityRecordRows : allRecordRows) {
            for (ActivityRecordRow activityRecordRow : activityRecordRows) {
                Row row = sheet.createRow(++rowIndex);

                Cell cell = row.createCell(0);
                cell.setCellValue(activityRecordRow.getTypeId());

                cell = row.createCell(1);
                cell.setCellValue(activityRecordRow.getTypeName());

                cell = row.createCell(2);
                cell.setCellValue(activityRecordRow.getActivityId());

                cell = row.createCell(3);
                cell.setCellValue(activityRecordRow.getActivityName());

                cell = row.createCell(4);
                cell.setCellValue(activityRecordRow.getRecordId());

                cell = row.createCell(5);
                cell.setCellValue(activityRecordRow.getProcessStatus().name());

                cell = row.createCell(6);
                cell.setCellValue(activityRecordRow.getMessage());

                cell = row.createCell(7);
                cell.setCellValue(activityRecordRow.getWhatsAppTemplateBody());

                cell = row.createCell(8);
                cell.setCellValue(activityRecordRow.isWhatsAppStatusSent() ? "Y" : "N");

                cell = row.createCell(9);
                cell.setCellValue(activityRecordRow.isWhatsAppStatusSkip() ? "Y" : "N");

                cell = row.createCell(10);
                cell.setCellValue(activityRecordRow.isWhatsAppStatusDelivered() ? "Y" : "N");

                cell = row.createCell(11);
                cell.setCellValue(activityRecordRow.isWhatsAppStatusRead() ? "Y" : "N");

                cell = row.createCell(12);
                cell.setCellValue(activityRecordRow.isWhatsAppStatusAcknowledge() ? "Y" : "N");

                cell = row.createCell(13);
                cell.setCellValue(activityRecordRow.isWhatsAppStatusFailed() ? "Y" : "N");

                cell = row.createCell(14);
                cell.setCellValue(activityRecordRow.getWhatsAppStatusFailedMessage());

                int columnIndex = 15;
                for (String key : activityRecordRow.getDynamicFields().keySet()) {
                    if (dynamicHeaderToIndexMap.containsKey(key)) {
                        cell = row.createCell(columnIndex + dynamicHeaderToIndexMap.get(key));
                        cell.setCellValue(activityRecordRow.getDynamicFields().get(key));
                    }
                }

            }
        }
    }
}
