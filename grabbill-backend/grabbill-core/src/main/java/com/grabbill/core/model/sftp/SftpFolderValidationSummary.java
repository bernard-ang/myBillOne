package com.grabbill.core.model.sftp;

import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author michaellow
 */
@Data
public class SftpFolderValidationSummary {

    private Map<Integer, List<IndexRowError>> indexRowErrors = new HashMap<>();
    private Map<String, String> fileErrors = new HashMap<>();
    private List<String> genericErrors = new ArrayList<>();


    public boolean hasError() {
        return !indexRowErrors.isEmpty() || !fileErrors.isEmpty() || !genericErrors.isEmpty();
    }

    @Data
    public static class IndexRowError {
        private int columnNo;
        private String error;
    }

    public static SftpFolderValidationSummary from(
            final boolean hasAttachment,
            final List<String> files,
            final ExcelIndexRows excelIndexRows,
            final int filenameColumnIndex
    ) {
        SftpFolderValidationSummary summary = new SftpFolderValidationSummary();
        if (excelIndexRows == null) {
            summary.getGenericErrors().add("Index row file (excel / csv) is not available");
        } else {
            if (hasAttachment) {
                Map<String, Integer> fileNameToCountMap = new HashMap<>();
                for (String file : files) {
                    if (fileNameToCountMap.containsKey(file)) {
                        fileNameToCountMap.put(file, fileNameToCountMap.get(file) + 1);
                    } else {
                        fileNameToCountMap.put(file, 1);
                    }
                }

                Set<String> filesWithoutIndexRow = new HashSet<>(fileNameToCountMap.keySet());
                for (ExcelIndexRow excelIndexRow : excelIndexRows.getRows()) {

                    // code cell error, row content not processed
                    if (!StringUtils.hasLength(excelIndexRow.getCodeError())) {
                        String fileName = getFilenameByColumnIndex(excelIndexRow, filenameColumnIndex);
                        if (fileNameToCountMap.containsKey(fileName)) {
                            int count = fileNameToCountMap.get(fileName);
                            if (count > 1) {
                                summary.getFileErrors().put(fileName, "More than one [" + fileName + "] were found");
                            }
                            filesWithoutIndexRow.remove(fileName);

                        } else {
                            summary.getFileErrors().put(fileName, "Missing attachment - [" + fileName + "]");
                        }
                    }
                }
                for (String key : filesWithoutIndexRow) {
                    summary.getFileErrors().put(key, "Missing index row - [" + key + "]");
                }

            } else {
                for (String file : files) {
                    summary.getFileErrors().put(file, "Invalid file - [" + file + "], type has no attachment");
                }
            }

            if (!excelIndexRows.getHearderErrors().isEmpty()) {
                summary.getGenericErrors().addAll(excelIndexRows.getHearderErrors());

            } else {
                for (ExcelIndexRow excelIndexRow : excelIndexRows.getRows()) {
                    if (StringUtils.hasLength(excelIndexRow.getCodeError())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(1);
                        error.setError(excelIndexRow.getCodeError());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError1())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(2);
                        error.setError(excelIndexRow.getError1());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError2())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(3);
                        error.setError(excelIndexRow.getError2());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError3())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(4);
                        error.setError(excelIndexRow.getError3());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError4())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(5);
                        error.setError(excelIndexRow.getError4());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError5())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(6);
                        error.setError(excelIndexRow.getError5());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError6())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(7);
                        error.setError(excelIndexRow.getError6());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError7())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(8);
                        error.setError(excelIndexRow.getError7());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError8())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(9);
                        error.setError(excelIndexRow.getError8());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError9())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(10);
                        error.setError(excelIndexRow.getError9());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                    if (StringUtils.hasLength(excelIndexRow.getError10())) {
                        if (!summary.getIndexRowErrors().containsKey(excelIndexRow.getSeqOrder())) {
                            summary.getIndexRowErrors().put(excelIndexRow.getSeqOrder(), new ArrayList<>());
                        }

                        SftpFolderValidationSummary.IndexRowError error = new SftpFolderValidationSummary.IndexRowError();
                        error.setColumnNo(11);
                        error.setError(excelIndexRow.getError10());
                        summary.getIndexRowErrors().get(excelIndexRow.getSeqOrder()).add(error);
                    }
                }
            }
        }

        return summary;
    }

    private static String getFilenameByColumnIndex(final ExcelIndexRow excelIndexRow, final int columnIndex) {
        if (columnIndex == 1) {
            return excelIndexRow.getText1();
        } else if (columnIndex == 2) {
            return excelIndexRow.getText2();
        } else if (columnIndex == 3) {
            return excelIndexRow.getText3();
        } else if (columnIndex == 4) {
            return excelIndexRow.getText4();
        } else if (columnIndex == 5) {
            return excelIndexRow.getText5();
        } else if (columnIndex == 6) {
            return excelIndexRow.getText6();
        } else if (columnIndex == 7) {
            return excelIndexRow.getText7();
        } else if (columnIndex == 8) {
            return excelIndexRow.getText8();
        } else if (columnIndex == 9) {
            return excelIndexRow.getText9();
        } else {
            return excelIndexRow.getText10();
        }
    }

}
