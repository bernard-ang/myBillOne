package com.grabbill.server.service;

/**
 * @author michaellow
 */
public interface JobScheduler {

    void runImmediateJobs();

    void runScheduleJobs();

}
