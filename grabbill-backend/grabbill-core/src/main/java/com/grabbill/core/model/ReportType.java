package com.grabbill.core.model;

/**
 * @author michael
 */
public enum ReportType {

    ACTIVITY_SUMMARY,

    ACTIVITY_RECORDS,

    URL_CLICK_SUMMARY,

    URL_CLICK_RECORDS;


    public static ReportType from(final String reportType) {
        for (ReportType type : ReportType.values()) {
            if (type.name().equals(reportType)) {
                return type;
            }
        }
        return null;
    }

}
