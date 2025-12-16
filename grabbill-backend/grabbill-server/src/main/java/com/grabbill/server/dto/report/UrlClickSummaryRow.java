package com.grabbill.server.dto.report;

import lombok.Data;

@Data
public class UrlClickSummaryRow {

    private long typeId;

    private String typeName;

    private long activityId;

    private String activityName;

    private String url;

    private int clickCount;

    private int uniqueClickCount;

}
