package com.grabbill.core.service;

/**
 * Tracks updates to activity completion and status counts (e.g. MT WA sent, delivered, skip, sck, read, failed count).
 */
public interface ActivityTrackingService {
    enum ActivityType {
        MT_WHATSAPP
    }

    void trackCompleted(ActivityType activityType, Long id);

    void trackCount(ActivityType activityType, Long id);
}
