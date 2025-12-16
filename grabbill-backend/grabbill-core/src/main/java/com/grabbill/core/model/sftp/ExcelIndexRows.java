package com.grabbill.core.model.sftp;

import com.grabbill.core.entity.BaseIndexField;
import com.grabbill.core.model.DataType;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Data
public class ExcelIndexRows {

    private List<String> hearderErrors = new ArrayList<>();

    private List<ExcelIndexRow> rows = new ArrayList<>();

    private static final DateTimeFormatter INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}");

    public static ExcelIndexRows fromExcel(
            final InputStream excelInputStream,
            final List<? extends BaseIndexField> indexFields,
            final String code
    ) throws ExcelIndexRowException {
        ExcelIndexRows excelIndexRows = new ExcelIndexRows();

        Sheet sheet;
        try {
            sheet = WorkbookFactory.create(excelInputStream).getSheetAt(0);
        } catch (Exception ex) {
            throw new ExcelIndexRowException("Failed to read excel workbook", ex);
        }

        List<BaseIndexField> applicableIndexFields =
                indexFields.stream().filter(BaseIndexField::isApplicable).collect(Collectors.toList());

        DataFormatter formatter = new DataFormatter();
        Row headerRow = sheet.getRow(0);
        for (int colNo = 0; colNo < headerRow.getLastCellNum(); colNo++) {

            String colHeader = formatter.formatCellValue(headerRow.getCell(colNo));
            if (colNo == 0) {
                if (!"code".equalsIgnoreCase(colHeader)) {
                    excelIndexRows.getHearderErrors().add("Header column [" + (colNo + 1) + "] should be [code]");
                }
            } else {
                if (colNo <= applicableIndexFields.size()) {
                    BaseIndexField indexField = applicableIndexFields.get(colNo - 1);
                    if (!indexField.getHeader().equals(colHeader)) {
                        excelIndexRows.getHearderErrors().add("Header column [" + (colNo + 1) + "] should be [" + indexField.getHeader() + "]");
                    }
                } else {
                    excelIndexRows.getHearderErrors().add("Header column [" + (colNo + 1) + "] exceeded maximum number of columns " + (applicableIndexFields.size() + 1));
                }
            }
        }

        if (excelIndexRows.getHearderErrors().isEmpty()) {

            for(int rowNo = 1; rowNo <= sheet.getLastRowNum(); ++rowNo) {

                Row currentRow = sheet.getRow(rowNo);
                ExcelIndexRow excelIndexRow = new ExcelIndexRow();
                excelIndexRow.setSeqOrder(rowNo + 1);

                Cell codeCell = currentRow.getCell(0);
                String codeCellValue = formatter.formatCellValue(codeCell);
                if (code != null && code.equals(codeCellValue)) {
                    for (int colNo = 1; colNo <= applicableIndexFields.size(); colNo++) {
                        BaseIndexField indexField = applicableIndexFields.get(colNo - 1);
                        Cell currentCell = currentRow.getCell(colNo);
                        String cellValue = formatter.formatCellValue(currentCell);

                        String columnHeader = formatter.formatCellValue(headerRow.getCell(colNo));
                        if (colNo == 1) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError1("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError1("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText1(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError1("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText1(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber1(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError1("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate1(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError1("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 2) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError2("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError2("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText2(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError2("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText2(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber2(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError2("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate2(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError2("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 3) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError3("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError3("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText3(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError3("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText3(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber3(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError3("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate3(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError3("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 4) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError4("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError4("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText4(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError4("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText4(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber4(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException | IllegalStateException e) {
                                    excelIndexRow.setError4("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate4(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError4("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 5) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError5("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError5("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText5(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError5("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText5(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber5(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError5("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate5(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError5("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 6) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError6("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError6("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText6(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError6("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText6(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber6(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError6("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate6(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError6("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 7) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError7("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError7("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText7(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType()) && currentCell != null) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError7("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText7(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType())) {
                                try {
                                    excelIndexRow.setNumber7(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError7("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate7(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError7("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 8) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError8("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError8("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText8(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError8("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText8(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber8(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError8("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate8(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError8("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 9) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError9("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError9("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText9(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError9("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText9(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber9(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError9("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate9(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError9("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        } else if (colNo == 10) {
                            if (DataType.EMAIL.equals(indexField.getDataType())) {
                                if (indexField.isRequired()) {
                                    if (!StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError10("Column [" + columnHeader + "] is required");
                                    } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                        excelIndexRow.setError10("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                                excelIndexRow.setText10(cellValue);

                            } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                    excelIndexRow.setError10("Column [" + columnHeader + "] is required");
                                }
                                excelIndexRow.setText10(cellValue);

                            } else if (DataType.NUMBER.equals(indexField.getDataType()) && currentCell != null) {
                                try {
                                    excelIndexRow.setNumber10(Double.valueOf(currentCell.getNumericCellValue()).intValue());
                                } catch (NumberFormatException e) {
                                    excelIndexRow.setError10("Column [" + columnHeader + "] is invalid");
                                }
                            } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                try {
                                    excelIndexRow.setDate10(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                } catch (DateTimeParseException e) {
                                    excelIndexRow.setError10("Column [" + columnHeader + "] is invalid");
                                }
                            }
                        }
                    }
                } else {
                    excelIndexRow.setCodeError("Invalid code [" + codeCellValue + "]");
                }

                excelIndexRows.getRows().add(excelIndexRow);
            }
        }

        return excelIndexRows;
    }

    public static ExcelIndexRows fromCsv(
            final InputStream csvInputStream,
            final List<? extends BaseIndexField> indexFields,
            final String csvSeparator,
            final String code
    ) throws ExcelIndexRowException {
        ExcelIndexRows excelIndexRows = new ExcelIndexRows();
        List<BaseIndexField> applicableIndexFields =
                indexFields.stream().filter(BaseIndexField::isApplicable).collect(Collectors.toList());

        CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(csvInputStream))
                .withCSVParser(new CSVParserBuilder().withSeparator(csvSeparator.charAt(0)).build())
                .build();
        try {
            List<String[]> rows = csvReader.readAll();

            String[] headerRow = rows.get(0);
            for (int colNo = 0; colNo < headerRow.length; colNo++) {

                String colHeader = headerRow[colNo];
                if (colNo == 0) {
                    if (!"code".equalsIgnoreCase(colHeader)) {
                        excelIndexRows.getHearderErrors().add("Header column [" + (colNo + 1) + "] should be [code]");
                    }
                } else {
                    if (colNo <= applicableIndexFields.size()) {
                        BaseIndexField indexField = applicableIndexFields.get(colNo - 1);
                        if (!indexField.getHeader().equals(colHeader)) {
                            excelIndexRows.getHearderErrors().add("Header column [" + (colNo + 1) + "] should be [" + indexField.getHeader() + "]");
                        }
                    } else {
                        excelIndexRows.getHearderErrors().add("Header column [" + (colNo + 1) + "] exceeded maximum number of columns " + (applicableIndexFields.size() + 1));
                    }
                }
            }

            if (excelIndexRows.getHearderErrors().isEmpty()) {

                for(int rowNo = 1; rowNo < rows.size(); ++rowNo) {

                    String[] currentRow = rows.get(rowNo);
                    ExcelIndexRow excelIndexRow = new ExcelIndexRow();
                    excelIndexRow.setSeqOrder(rowNo + 1);

                    String codeCellValue = currentRow[0];
                    if (code != null && code.equals(codeCellValue)) {
                        for (int colNo = 1; colNo <= applicableIndexFields.size(); colNo++) {
                            BaseIndexField indexField = applicableIndexFields.get(colNo - 1);
                            String columnHeader = headerRow[colNo];
                            String cellValue = currentRow[colNo];

                            if (colNo == 1) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError1("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError1("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText1(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError1("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText1(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber1(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError1("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && cellValue != null && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate1(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError1("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 2) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError2("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError2("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText2(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError2("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText2(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber2(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError2("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate2(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError2("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 3) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError3("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError3("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText3(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError3("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText3(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber3(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError3("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate3(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError3("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 4) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError4("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError4("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText4(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError4("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText4(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber4(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException | IllegalStateException e) {
                                        excelIndexRow.setError4("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate4(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError4("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 5) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError5("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError5("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText5(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError5("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText5(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber5(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError5("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate5(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError5("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 6) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError6("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError6("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText6(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError6("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText6(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber6(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError6("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate6(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError6("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 7) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError7("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError7("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText7(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError7("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText7(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber7(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError7("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate7(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError7("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 8) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError8("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError8("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText8(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError8("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText8(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber8(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError8("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate8(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError8("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 9) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError9("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError9("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText9(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError9("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText9(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber9(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError9("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate9(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError9("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            } else if (colNo == 10) {
                                if (DataType.EMAIL.equals(indexField.getDataType())) {
                                    if (indexField.isRequired()) {
                                        if (!StringUtils.hasLength(cellValue)) {
                                            excelIndexRow.setError10("Column [" + columnHeader + "] is required");
                                        } else if (!EMAIL_PATTERN.matcher(cellValue).matches()) {
                                            excelIndexRow.setError10("Column [" + columnHeader + "] is invalid");
                                        }
                                    }
                                    excelIndexRow.setText10(cellValue);

                                } else if (DataType.TEXT.equals(indexField.getDataType())) {
                                    if (indexField.isRequired() && !StringUtils.hasLength(cellValue)) {
                                        excelIndexRow.setError10("Column [" + columnHeader + "] is required");
                                    }
                                    excelIndexRow.setText10(cellValue);

                                } else if (DataType.NUMBER.equals(indexField.getDataType()) && cellValue != null) {
                                    try {
                                        excelIndexRow.setNumber10(Integer.parseInt(cellValue));
                                    } catch (NumberFormatException e) {
                                        excelIndexRow.setError10("Column [" + columnHeader + "] is invalid");
                                    }
                                } else if (DataType.DATE.equals(indexField.getDataType()) && !cellValue.isEmpty()) {
                                    try {
                                        excelIndexRow.setDate10(LocalDate.from(INPUT_DATE_FORMAT.parse(cellValue)));
                                    } catch (DateTimeParseException e) {
                                        excelIndexRow.setError10("Column [" + columnHeader + "] is invalid");
                                    }
                                }
                            }
                        }

                    } else {
                        excelIndexRow.setCodeError("Invalid code [" + codeCellValue + "]");
                    }
                    excelIndexRows.getRows().add(excelIndexRow);
                }

            }

        } catch (IOException | CsvException e) {
            throw new ExcelIndexRowException("Failed to read csv file!", e);
        }

        return excelIndexRows;
    }

}
