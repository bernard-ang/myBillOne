package com.grabbill.engine.job.dsn;

import com.grabbill.core.model.job.event.DsnScanJobEvent;

/**
 * @author michaellow
 */
public interface DsnScanJobEventPublisher {

    void publish(DsnScanJobEvent dsnScanJobEvent);

}
