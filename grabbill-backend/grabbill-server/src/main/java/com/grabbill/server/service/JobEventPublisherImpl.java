package com.grabbill.server.service;

import com.grabbill.core.entity.Job;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.job.event.JobEvent;
import com.grabbill.core.model.job.event.JobEventType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Default {@link JobEventPublisher} implementation.
 *
 * @author michaellow
 */
@Slf4j
public class JobEventPublisherImpl implements JobEventPublisher {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private Queue jobQueue;

    @Autowired
    private JobService jobService;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Override
    public void publish(final Job job) {
        // mark job instance as QUEUED (won't be pick up by job scheduler again)
        job.setStatus(JobStatus.QUEUED);
        jobService.saveAndFlush(job);

        // publish job event to queue
        JobEvent jobEvent = new JobEvent();
        jobEvent.setJobId(job.getId());
        jobEvent.setType(job.getEventType());
        jobEvent.setDomainType(job.getDomainType());
        jobEvent.setActivityId(job.getActivityId());
        jobEvent.setDateTime(OffsetDateTime.now(ZoneOffset.UTC));
        rabbitTemplate.convertAndSend(jobQueue.getName(), jobEvent);

        log.info("Job event published - " + jobEvent.toString());
        auditLogService.log(
                job.getAccount().getId(),
                Optional.empty(),
                job.getActivityId(),
                job.getDomainType(),
                ActionType.JOB_SUBMIT,
                job.getActivityName(),
                "system"
        );
    }

}
