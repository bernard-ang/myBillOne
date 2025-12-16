package com.grabbill.server.service;

import com.grabbill.core.entity.Job;
import com.grabbill.core.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author michaellow
 */
@Slf4j
@Transactional
public class JobSchedulerImpl implements JobScheduler {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobEventPublisher jobEventPublisher;


    @Override
    public void runImmediateJobs() {
        log.debug("JobScheduler starts scanning for 'immediate' jobs.");

        int count = 0;
        for (Job job : jobService.getAllImmediateJobs()) {
            jobEventPublisher.publish(job);
            count++;
        }

        log.debug("Total " + count + " 'immediate' jobs been submitted for execution.");
    }

    @Override
    public void runScheduleJobs() {
        log.debug("JobScheduler starts scanning for 'schedule ready' jobs.");

        int count = 0;
        for (Job job : jobService.getAllScheduleReadyJobs()) {
            jobEventPublisher.publish(job);
            count++;
        }

        log.debug("Total " + count + " 'schedule ready' jobs been submitted for execution.");
    }

}
