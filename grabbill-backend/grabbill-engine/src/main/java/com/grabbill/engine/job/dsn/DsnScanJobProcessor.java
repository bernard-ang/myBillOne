package com.grabbill.engine.job.dsn;

import com.grabbill.core.model.job.event.DsnScanJobEvent;
import com.grabbill.engine.job.JobProcessingException;

/**
 * @author michaellow
 */
public interface DsnScanJobProcessor {

    String CREATED_BY = "system";

    void process(DsnScanJobEvent dsnScanJobEvent) throws JobProcessingException;

}
