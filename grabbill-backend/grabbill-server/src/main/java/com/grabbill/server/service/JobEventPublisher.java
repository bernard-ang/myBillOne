package com.grabbill.server.service;

import com.grabbill.core.entity.Job;

/**
 * @author michaellow
 */
public interface JobEventPublisher {

    void publish(Job job);

}
