package com.grabbill.server.dto.report;

import lombok.Data;

import java.time.ZonedDateTime;

/**
 * @author michaellow
 */
@Data
public class ActivitySummaryRow {

    private long typeId;

    private String typeName;

    private long activityId;

    private String activityName;

    private ZonedDateTime startAt;

    private ZonedDateTime completedAt;

    private int totalEmails;

    private int totalSent;

    private int totalUnsubscribed;

    private int totalBounced;
    private int totalSkip;

    private int totalOpened;

    private int totalWhatsApp;
    private int totalWhatsAppSent;
    private int totalWhatsAppSkip;
    private int totalWhatsAppRead;
    private int totalWhatsAppDelivered;
    private int totalWhatsAppAcknowledge;
    private int totalWhatsAppFailed;

}
