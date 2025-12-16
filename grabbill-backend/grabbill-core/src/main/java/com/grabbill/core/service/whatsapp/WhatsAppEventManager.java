package com.grabbill.core.service.whatsapp;

/**
 * @author seez
 */
public interface WhatsAppEventManager {

    void process();

    /**
     * @deprecated once ActivityTrackingService is stable, this should be removed.
     */
    void updateCount();
}
