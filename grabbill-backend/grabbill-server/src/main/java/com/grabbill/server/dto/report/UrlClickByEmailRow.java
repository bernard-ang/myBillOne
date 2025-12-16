package com.grabbill.server.dto.report;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class UrlClickByEmailRow {

    private long typeId;

    private String typeName;

    private long activityId;

    private String activityName;

    private ZonedDateTime clickedAt;

    private String url;

    private String email;

    private int clickCount;

}
