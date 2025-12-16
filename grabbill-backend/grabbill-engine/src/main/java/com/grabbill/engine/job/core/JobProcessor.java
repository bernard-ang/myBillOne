package com.grabbill.engine.job.core;

import com.grabbill.core.model.job.event.JobEvent;
import com.grabbill.core.model.DomainType;
import com.grabbill.engine.job.JobProcessingException;

/**
 * @author michaellow
 */
public interface JobProcessor {

    String CREATED_BY = "system";


    DomainType getSupportedActivityType();

    void process(JobEvent jobEvent) throws JobProcessingException;

}
