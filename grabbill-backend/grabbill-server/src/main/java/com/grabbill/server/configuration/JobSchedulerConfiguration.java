package com.grabbill.server.configuration;

import com.grabbill.server.service.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author michaellow
 */
@EnableScheduling
@ConditionalOnProperty(name = "job.scheduler.enabled")
public class JobSchedulerConfiguration {

    @Autowired
    private JobScheduler jobScheduler;


    @Scheduled(fixedDelayString = "${job.scheduler.immediate-rate}")
    public void runImmediateJobs() {
        jobScheduler.runImmediateJobs();
    }

    @Scheduled(fixedDelayString = "${job.scheduler.schedule-rate}")
    public void runScheduleJobs() {
        jobScheduler.runScheduleJobs();
    }

}
