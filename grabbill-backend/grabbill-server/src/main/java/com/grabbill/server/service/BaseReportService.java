package com.grabbill.server.service;

import com.grabbill.core.entity.BaseIndexRow;
import com.grabbill.core.model.BounceType;
import com.grabbill.core.model.DataType;
import com.grabbill.server.dto.report.ActivityRecordRow;
import com.grabbill.server.dto.report.ActivitySummaryRow;
import com.grabbill.server.dto.report.UrlClickByEmailRow;
import com.grabbill.server.dto.report.UrlClickSummaryRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author michaellow
 */
public class BaseReportService {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    protected String getValue(
            final int i,
            final BaseIndexRow indexRow,
            final DataType indexFieldDataType
    ) {
        String value = null;
        if (i == 0) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText1();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber1() != null ? indexRow.getNumber1().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate1() != null ? DATE_FORMATTER.format(indexRow.getDate1()) : "";
            }

        } else if (i == 1) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText2();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber2() != null ? indexRow.getNumber2().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate2() != null ? DATE_FORMATTER.format(indexRow.getDate2()) : "";
            }

        } else if (i == 2) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText3();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber3() != null ? indexRow.getNumber3().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate3() != null ? DATE_FORMATTER.format(indexRow.getDate3()) : "";
            }

        } else if (i == 3) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText4();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber4() != null ? indexRow.getNumber4().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate4() != null ? DATE_FORMATTER.format(indexRow.getDate4()) : "";
            }

        } else if (i == 4) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText5();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber5() != null ? indexRow.getNumber5().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate5() != null ? DATE_FORMATTER.format(indexRow.getDate5()) : "";
            }

        } else if (i == 5) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText6();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber6() != null ? indexRow.getNumber6().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate6() != null ? DATE_FORMATTER.format(indexRow.getDate6()) : "";
            }

        } else if (i == 6) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText7();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber7() != null ? indexRow.getNumber7().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate7() != null ? DATE_FORMATTER.format(indexRow.getDate7()) : "";
            }

        } else if (i == 7) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText8();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber8() != null ? indexRow.getNumber8().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate8() != null ? DATE_FORMATTER.format(indexRow.getDate8()) : "";
            }

        } else if (i == 8) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText9();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber9() != null ? indexRow.getNumber9().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate9() != null ? DATE_FORMATTER.format(indexRow.getDate9()) : "";
            }

        } else if (i == 9) {
            if (DataType.TEXT.equals(indexFieldDataType) || DataType.EMAIL.equals(indexFieldDataType)) {
                value = indexRow.getText10();
            } else if (DataType.NUMBER.equals(indexFieldDataType)) {
                value = indexRow.getNumber10() != null ? indexRow.getNumber10().toString() : "";
            } else if (DataType.DATE.equals(indexFieldDataType)) {
                value = indexRow.getDate10() != null ? DATE_FORMATTER.format(indexRow.getDate10()) : "";
            }
        }

        return value;
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
        sheet.setColumnWidth(9, 5000);
        sheet.setColumnWidth(10, 4000);
        sheet.setColumnWidth(11, 4000);
        if (hasWhatsApp) {
            sheet.setColumnWidth(12, 5000);
            sheet.setColumnWidth(13, 5000);
            sheet.setColumnWidth(14, 6000);
            sheet.setColumnWidth(15, 5000);
            sheet.setColumnWidth(16, 6000);
            sheet.setColumnWidth(17, 5500);
        }

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
        headerCell.setCellValue("Total Emails");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(7);
        headerCell.setCellValue("Total Sent");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(8);
        headerCell.setCellValue("Total Skip");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(9);
        headerCell.setCellValue("Total Unsubscribed");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(10);
        headerCell.setCellValue("Total Bounced");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(11);
        headerCell.setCellValue("Total Opened");
        headerCell.setCellStyle(headerCellStyle);

        if (hasWhatsApp) {
            headerCell = hearderRow.createCell(12);
            headerCell.setCellValue("Total WhatsApp Sent");
            headerCell.setCellStyle(headerCellStyle);

            headerCell = hearderRow.createCell(13);
            headerCell.setCellValue("Total WhatsApp Skip");
            headerCell.setCellStyle(headerCellStyle);

            headerCell = hearderRow.createCell(14);
            headerCell.setCellValue("Total WhatsApp Delivered");
            headerCell.setCellStyle(headerCellStyle);

            headerCell = hearderRow.createCell(15);
            headerCell.setCellValue("Total WhatsApp Read");
            headerCell.setCellStyle(headerCellStyle);

            headerCell = hearderRow.createCell(16);
            headerCell.setCellValue("Total WhatsApp Acknowledged");
            headerCell.setCellStyle(headerCellStyle);

            headerCell = hearderRow.createCell(17);
            headerCell.setCellValue("Total WhatsApp Failed");
            headerCell.setCellStyle(headerCellStyle);
        }

        int totalEmails = 0;
        int totalSent = 0;
        int totalSkip = 0;
        int totalUnsubscribed = 0;
        int totalBounced = 0;
        int totalOpened = 0;
        int totalWhatsAppSent = 0;
        int totalWhatsAppSkip = 0;
        int totalWhatsAppDelivered = 0;
        int totalWhatsAppRead = 0;
        int totalWhatsAppAcknowledged = 0;
        int totalWhatsAppFailed = 0;

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
            cell.setCellValue(DATE_TIME_FORMATTER.format(activitySummaryRow.getStartAt()));

            cell = row.createCell(5);
            cell.setCellValue(DATE_TIME_FORMATTER.format(activitySummaryRow.getCompletedAt()));

            cell = row.createCell(6);
            cell.setCellValue(activitySummaryRow.getTotalEmails());
            totalEmails += activitySummaryRow.getTotalEmails();

            cell = row.createCell(7);
            cell.setCellValue(activitySummaryRow.getTotalSent());
            totalSent += activitySummaryRow.getTotalSent();

            cell = row.createCell(8);
            cell.setCellValue(activitySummaryRow.getTotalSkip());
            totalSkip += activitySummaryRow.getTotalSkip();

            cell = row.createCell(9);
            cell.setCellValue(activitySummaryRow.getTotalUnsubscribed());
            totalUnsubscribed += activitySummaryRow.getTotalUnsubscribed();

            cell = row.createCell(10);
            cell.setCellValue(activitySummaryRow.getTotalBounced());
            totalBounced += activitySummaryRow.getTotalBounced();

            cell = row.createCell(11);
            cell.setCellValue(activitySummaryRow.getTotalOpened());
            totalOpened += activitySummaryRow.getTotalOpened();

            if (hasWhatsApp) {
                cell = row.createCell(12);
                cell.setCellValue(activitySummaryRow.getTotalWhatsAppSent());
                totalWhatsAppSent += activitySummaryRow.getTotalWhatsAppSent();

                cell = row.createCell(13);
                cell.setCellValue(activitySummaryRow.getTotalWhatsAppSkip());
                totalWhatsAppSkip += activitySummaryRow.getTotalWhatsAppSkip();

                cell = row.createCell(14);
                cell.setCellValue(activitySummaryRow.getTotalWhatsAppDelivered());
                totalWhatsAppDelivered += activitySummaryRow.getTotalWhatsAppDelivered();

                cell = row.createCell(15);
                cell.setCellValue(activitySummaryRow.getTotalWhatsAppRead());
                totalWhatsAppRead += activitySummaryRow.getTotalWhatsAppRead();

                cell = row.createCell(16);
                cell.setCellValue(activitySummaryRow.getTotalWhatsAppAcknowledge());
                totalWhatsAppAcknowledged += activitySummaryRow.getTotalWhatsAppAcknowledge();

                cell = row.createCell(17);
                cell.setCellValue(activitySummaryRow.getTotalWhatsAppFailed());
                totalWhatsAppFailed += activitySummaryRow.getTotalWhatsAppFailed();
            }
        }

        rowIndex = rowIndex + 2;
        Row row = sheet.createRow(rowIndex);
        CellStyle summaryCellStyle = createSummaryCellStyle(workbook);

        Cell cell = row.createCell(5);
        cell.setCellValue("Total");
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(6);
        cell.setCellValue(totalEmails);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(7);
        cell.setCellValue(totalSent);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(8);
        cell.setCellValue(totalSkip);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(9);
        cell.setCellValue(totalUnsubscribed);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(10);
        cell.setCellValue(totalBounced);
        cell.setCellStyle(summaryCellStyle);

        cell = row.createCell(11);
        cell.setCellValue(totalOpened);
        cell.setCellStyle(summaryCellStyle);

        if (hasWhatsApp) {
            cell = row.createCell(12);
            cell.setCellValue(totalWhatsAppSent);
            cell.setCellStyle(summaryCellStyle);

            cell = row.createCell(13);
            cell.setCellValue(totalWhatsAppSkip);
            cell.setCellStyle(summaryCellStyle);

            cell = row.createCell(14);
            cell.setCellValue(totalWhatsAppDelivered);
            cell.setCellStyle(summaryCellStyle);

            cell = row.createCell(15);
            cell.setCellValue(totalWhatsAppRead);
            cell.setCellStyle(summaryCellStyle);

            cell = row.createCell(16);
            cell.setCellValue(totalWhatsAppAcknowledged);
            cell.setCellStyle(summaryCellStyle);

            cell = row.createCell(17);
            cell.setCellValue(totalWhatsAppFailed);
            cell.setCellStyle(summaryCellStyle);
        }

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
        headerCell.setCellValue("Sent");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(8, 5000);
        headerCell = hearderRow.createCell(8);
        headerCell.setCellValue("Skipped (Unsubscribed)");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(9, 5000);
        headerCell = hearderRow.createCell(9);
        headerCell.setCellValue("Skipped (Bounced)");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(10, 4000);
        headerCell = hearderRow.createCell(10);
        headerCell.setCellValue("Bounce Type");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(11, 4000);
        headerCell = hearderRow.createCell(11);
        headerCell.setCellValue("DSN Received");
        headerCell.setCellStyle(headerCellStyle);

        sheet.setColumnWidth(12, 4000);
        headerCell = hearderRow.createCell(12);
        headerCell.setCellValue("DSN Message");
        headerCell.setCellStyle(headerCellStyle);

        int columnIndex = 13;
        if (!dynamicHeaders.isEmpty()) {
            for (String dynamicHeader : dynamicHeaders) {
                sheet.setColumnWidth(columnIndex, 4000);
                headerCell = hearderRow.createCell(columnIndex++);
                headerCell.setCellValue(dynamicHeader + "*");
                headerCell.setCellStyle(headerCellStyle);
            }
        }

        if (hasWhatsApp) {
            sheet.setColumnWidth(columnIndex, 6000);
            headerCell = hearderRow.createCell(columnIndex++);
            headerCell.setCellValue("WhatsApp Body Content");
            headerCell.setCellStyle(headerCellStyle);

            sheet.setColumnWidth(columnIndex, 5000);
            headerCell = hearderRow.createCell(columnIndex++);
            headerCell.setCellValue("WhatsApp Sent");
            headerCell.setCellStyle(headerCellStyle);

            sheet.setColumnWidth(columnIndex, 5000);
            headerCell = hearderRow.createCell(columnIndex++);
            headerCell.setCellValue("WhatsApp Skipped");
            headerCell.setCellStyle(headerCellStyle);

            sheet.setColumnWidth(columnIndex, 5000);
            headerCell = hearderRow.createCell(columnIndex++);
            headerCell.setCellValue("WhatsApp Delivered");
            headerCell.setCellStyle(headerCellStyle);

            sheet.setColumnWidth(columnIndex, 5000);
            headerCell = hearderRow.createCell(columnIndex++);
            headerCell.setCellValue("WhatsApp Read");
            headerCell.setCellStyle(headerCellStyle);

            sheet.setColumnWidth(columnIndex, 6000);
            headerCell = hearderRow.createCell(columnIndex++);
            headerCell.setCellValue("WhatsApp Acknowledged");
            headerCell.setCellStyle(headerCellStyle);

            sheet.setColumnWidth(columnIndex, 5000);
            headerCell = hearderRow.createCell(columnIndex++);
            headerCell.setCellValue("WhatsApp Failed");
            headerCell.setCellStyle(headerCellStyle);

            sheet.setColumnWidth(columnIndex, 6000);
            headerCell = hearderRow.createCell(columnIndex);
            headerCell.setCellValue("WhatsApp Failed Message");
            headerCell.setCellStyle(headerCellStyle);
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
                cell.setCellValue(activityRecordRow.isEmailStatusSent() ? "Y" : "N");

                cell = row.createCell(8);
                cell.setCellValue(activityRecordRow.isEmailStatusSkipUnsubscribed() ? "Y" : "N");

                cell = row.createCell(9);
                cell.setCellValue(activityRecordRow.isEmailStatusSkipBounced() ? "Y" : "N");

                cell = row.createCell(10);
                String bounceType = "";
                if (activityRecordRow.isEmailStatusSoftBounce()) {
                    bounceType = BounceType.SOFT_BOUNCE.name();
                } else if (activityRecordRow.isEmailStatusHardBounce()) {
                    bounceType = BounceType.HARD_BOUNCE.name();
                }
                cell.setCellValue(bounceType);

                cell = row.createCell(11);
                cell.setCellValue(activityRecordRow.isEmailDsnReceivedConfirmation() ? "Y" : "N");

                cell = row.createCell(12);
                cell.setCellValue(activityRecordRow.getEmailDsnMessage());

                columnIndex = 13;
                for (String key : activityRecordRow.getDynamicFields().keySet()) {
                    cell = row.createCell(columnIndex++);
                    if (dynamicHeaderToIndexMap.containsKey(key)) {
                        cell.setCellValue(activityRecordRow.getDynamicFields().get(key));
                    }
                }

                if (hasWhatsApp) {
                    cell = row.createCell(columnIndex++);
                    cell.setCellValue(activityRecordRow.getWhatsAppTemplateBody());

                    cell = row.createCell(columnIndex++);
                    cell.setCellValue(activityRecordRow.isWhatsAppStatusSent() ? "Y" : "N");

                    cell = row.createCell(columnIndex++);
                    cell.setCellValue(activityRecordRow.isWhatsAppStatusSkip() ? "Y" : "N");

                    cell = row.createCell(columnIndex++);
                    cell.setCellValue(activityRecordRow.isWhatsAppStatusDelivered() ? "Y" : "N");

                    cell = row.createCell(columnIndex++);
                    cell.setCellValue(activityRecordRow.isWhatsAppStatusRead() ? "Y" : "N");

                    cell = row.createCell(columnIndex++);
                    cell.setCellValue(activityRecordRow.isWhatsAppStatusAcknowledge() ? "Y" : "N");

                    cell = row.createCell(columnIndex++);
                    cell.setCellValue(activityRecordRow.isWhatsAppStatusFailed() ? "Y" : "N");

                    cell = row.createCell(columnIndex);
                    cell.setCellValue(activityRecordRow.getWhatsAppStatusFailedMessage());
                }
            }
        }
    }

    protected void buildUrlClickSummarySheet(
            final Workbook workbook,
            final List<UrlClickSummaryRow> urlClickSummaryRows
    ) {
        Sheet sheet = workbook.createSheet("url click summary");
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 8000);
        sheet.setColumnWidth(5, 4000);
        sheet.setColumnWidth(6, 4000);

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
        headerCell.setCellValue("URL");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(5);
        headerCell.setCellValue("Unique Click");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(6);
        headerCell.setCellValue("Click");
        headerCell.setCellStyle(headerCellStyle);

        for (UrlClickSummaryRow urlClickSummaryRow : urlClickSummaryRows) {

            Row row = sheet.createRow(++rowIndex);

            Cell cell = row.createCell(0);
            cell.setCellValue(urlClickSummaryRow.getTypeId());

            cell = row.createCell(1);
            cell.setCellValue(urlClickSummaryRow.getTypeName());

            cell = row.createCell(2);
            cell.setCellValue(urlClickSummaryRow.getActivityId());

            cell = row.createCell(3);
            cell.setCellValue(urlClickSummaryRow.getActivityName());

            cell = row.createCell(4);
            cell.setCellValue(urlClickSummaryRow.getUrl());

            cell = row.createCell(5);
            cell.setCellValue(urlClickSummaryRow.getUniqueClickCount());

            cell = row.createCell(6);
            cell.setCellValue(urlClickSummaryRow.getClickCount());

        }
    }

    protected void buildUrlClickByEmailSheet(
            final Workbook workbook,
            final List<UrlClickByEmailRow> urlClickByEmailRows
    ) {
        Sheet sheet = workbook.createSheet("url click records");
        sheet.setColumnWidth(0, 4000);
        sheet.setColumnWidth(1, 4000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 4000);
        sheet.setColumnWidth(4, 8000);
        sheet.setColumnWidth(5, 8000);
        sheet.setColumnWidth(6, 8000);
        sheet.setColumnWidth(7, 4000);

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
        headerCell.setCellValue("Clicked At");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(5);
        headerCell.setCellValue("URL");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(6);
        headerCell.setCellValue("Email");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(7);
        headerCell.setCellValue("Click");
        headerCell.setCellStyle(headerCellStyle);

        for (UrlClickByEmailRow urlClickByEmailRow : urlClickByEmailRows) {

            Row row = sheet.createRow(++rowIndex);

            Cell cell = row.createCell(0);
            cell.setCellValue(urlClickByEmailRow.getTypeId());

            cell = row.createCell(1);
            cell.setCellValue(urlClickByEmailRow.getTypeName());

            cell = row.createCell(2);
            cell.setCellValue(urlClickByEmailRow.getActivityId());

            cell = row.createCell(3);
            cell.setCellValue(urlClickByEmailRow.getActivityName());

            cell = row.createCell(4);
            cell.setCellValue(DATE_TIME_FORMATTER.format(urlClickByEmailRow.getClickedAt()));

            cell = row.createCell(5);
            cell.setCellValue(urlClickByEmailRow.getUrl());

            cell = row.createCell(6);
            cell.setCellValue(urlClickByEmailRow.getEmail());

            cell = row.createCell(7);
            cell.setCellValue(urlClickByEmailRow.getClickCount());

        }
    }

    protected CellStyle createHeaderCellStyle(final Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setBold(true);
        headerStyle.setFont(font);

        return headerStyle;
    }

    protected CellStyle createSummaryCellStyle(final Workbook workbook) {
        CellStyle summaryCellStyle = workbook.createCellStyle();
        summaryCellStyle.setBorderTop(BorderStyle.MEDIUM);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setBold(true);
        summaryCellStyle.setFont(font);

        return summaryCellStyle;
    }

}
