package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.ReportType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author michaellow
 */
@Slf4j
public class DigitalFilingReportServiceImpl extends BaseReportService implements DigitalFilingReportService {

    @Override
    public Workbook generateDigitalFilingReport(
            final List<DigitalFilingActivity> activities,
            final List<ReportType> reportTypes,
            final ZoneId zoneId
    ) {
        Workbook workbook = new XSSFWorkbook();
        for (ReportType reportType : reportTypes) {
            if (ReportType.ACTIVITY_SUMMARY.equals(reportType)) {

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
                headerCell.setCellValue("Total Files");
                headerCell.setCellStyle(headerCellStyle);

                headerCell = hearderRow.createCell(7);
                headerCell.setCellValue("Total Storage");
                headerCell.setCellStyle(headerCellStyle);

                headerCell = hearderRow.createCell(8);
                headerCell.setCellValue("Total Records");
                headerCell.setCellStyle(headerCellStyle);


                int allStorageSize = 0;
                int allFiles = 0;
                int allRecords = 0;
                for (DigitalFilingActivity activity : activities) {
                    DigitalFilingType type = activity.getDigitalFilingType();

                    Row row = sheet.createRow(++rowIndex);

                    Cell cell = row.createCell(0);
                    cell.setCellValue(type.getId());

                    cell = row.createCell(1);
                    cell.setCellValue(type.getName());

                    cell = row.createCell(2);
                    cell.setCellValue(activity.getId());

                    cell = row.createCell(3);
                    cell.setCellValue(activity.getName());

                    cell = row.createCell(4);
                    cell.setCellValue(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(activity.getSubmittedTimestamp().toInstant(), zoneId)));

                    cell = row.createCell(5);
                    cell.setCellValue(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(activity.getProcessedTimestamp().toInstant(), zoneId)));

                    cell = row.createCell(6);
                    cell.setCellValue(activity.getDigitalFilingFiles().size());
                    allFiles += activity.getDigitalFilingFiles().size();

                    cell = row.createCell(7);
                    cell.setCellValue(activity.getStorageSize());
                    allStorageSize += activity.getStorageSize();

                    cell = row.createCell(8);
                    cell.setCellValue(activity.getDigitalFilingRecords().size());
                    allRecords += activity.getDigitalFilingRecords().size();
                }

                rowIndex = rowIndex + 2;
                Row row = sheet.createRow(rowIndex);
                CellStyle summaryCellStyle = createSummaryCellStyle(workbook);

                Cell cell = row.createCell(5);
                cell.setCellValue("Total");
                cell.setCellStyle(summaryCellStyle);

                cell = row.createCell(6);
                cell.setCellValue(allFiles);
                cell.setCellStyle(summaryCellStyle);

                cell = row.createCell(7);
                cell.setCellValue(allStorageSize);
                cell.setCellStyle(summaryCellStyle);

                cell = row.createCell(8);
                cell.setCellValue(allRecords);
                cell.setCellStyle(summaryCellStyle);

            } else if (ReportType.ACTIVITY_RECORDS.equals(reportType)) {

                // generate header
                Set<String> dynamicHeaders = new LinkedHashSet<>();
                for (DigitalFilingActivity activity : activities) {
                    DigitalFilingType type = activity.getDigitalFilingType();

                    List<DigitalFilingIndexField> indexFields = type.getDigitalFilingIndexFields();
                    for (DigitalFilingIndexField indexField : indexFields) {
                        dynamicHeaders.add(indexField.getLabel());
                    }
                }
                Map<String, Integer> dynamicHeaderToIndexMap = new HashMap<>();
                int dynamicHeaderIndex = 0;
                for (String dynamicHeader : dynamicHeaders) {
                    dynamicHeaderToIndexMap.put(dynamicHeader, dynamicHeaderIndex++);
                }

                Sheet sheet = workbook.createSheet("activity records");
                int rowIndex = 0;
                Row hearderRow = sheet.createRow(rowIndex > 0 ? ++rowIndex : rowIndex);
                CellStyle headerCellStyle = createHeaderCellStyle(workbook);
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

                if (!dynamicHeaders.isEmpty()) {
                    int columnIndex = 6;
                    for (String dynamicHeader : dynamicHeaders) {
                        sheet.setColumnWidth(columnIndex, 4000);
                        headerCell = hearderRow.createCell(columnIndex++);
                        headerCell.setCellValue(dynamicHeader + "*");
                        headerCell.setCellStyle(headerCellStyle);
                    }
                }

                for (DigitalFilingActivity activity : activities) {
                    DigitalFilingType type = activity.getDigitalFilingType();
                    List<DigitalFilingIndexField> indexFields = type.getDigitalFilingIndexFields();

                    for (DigitalFilingRecord record : activity.getDigitalFilingRecords()) {
                        DigitalFilingIndexRow indexRow = record.getDigitalFilingIndexRow();
                        Row row = sheet.createRow(++rowIndex);

                        Cell cell = row.createCell(0);
                        cell.setCellValue(type.getId());

                        cell = row.createCell(1);
                        cell.setCellValue(type.getName());

                        cell = row.createCell(2);
                        cell.setCellValue(activity.getId());

                        cell = row.createCell(3);
                        cell.setCellValue(activity.getName());

                        cell = row.createCell(4);
                        cell.setCellValue(record.getId());

                        cell = row.createCell(5);
                        cell.setCellValue(record.getStatus().name());

                        int columnIndex = 6;
                        for (int i = 0; i < indexFields.size(); i++) {
                            DigitalFilingIndexField indexField = indexFields.get(i);
                            String key = indexField.getLabel();
                            if (dynamicHeaderToIndexMap.containsKey(key)) {
                                cell = row.createCell(columnIndex + dynamicHeaderToIndexMap.get(key));
                                cell.setCellValue(getValue(i, indexRow, indexField.getDataType()));
                            }
                        }

                    }
                }

            } else {
                log.warn("Report type of " + reportType + " is not supported for Digital Filing Type!");
            }
        }

        return workbook;
    }

}
