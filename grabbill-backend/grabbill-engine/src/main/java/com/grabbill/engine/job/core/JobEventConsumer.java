package com.grabbill.engine.job.core;

import com.grabbill.core.model.job.event.JobEvent;

/**
 * @author michaellow
 */
public interface JobEventConsumer {

    void consume(JobEvent jobEvent);

}
