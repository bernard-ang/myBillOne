package com.grabbill.engine.job.dsn;

import com.grabbill.core.model.job.event.DsnScanJobEvent;

/**
 * @author michaellow
 */
public interface DsnScanJobEventConsumer {

    void consume(DsnScanJobEvent dsnScanJobEvent);

}
