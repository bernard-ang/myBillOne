package com.grabbill.engine.job.core;

import com.grabbill.core.entity.Job;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.model.job.event.JobEvent;
import com.grabbill.core.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Optional;

/**
 * @author michaellow
 */
@Slf4j
public class JobEventConsumerImpl implements JobEventConsumer {

    @Autowired
    private JobService jobService;

    @Autowired
    private DigitalFilingJobProcessor digitalFilingJobProcessor;

    @Autowired
    private EmailCampaignJobProcessor emailCampaignJobProcessor;

    @Autowired
    private TransactionalEmailJobProcessor transactionalEmailJobProcessor;

    @Autowired
    private MTTransactionalEmailJobProcessor mtTransactionalEmailJobProcessor;

    @Autowired
    private SmsJobProcessor smsJobProcessor;

    @Autowired
    private WhatsAppJobProcessor whatsAppJobProcessor;

    @Autowired
    private MTWhatsAppJobProcessor mtWhatsAppJobProcessor;


    @RabbitListener(queuesToDeclare = {
            @Queue(name = "${job.queue.name}", durable = "true")
    })
    @Override
    public void consume(@Payload final JobEvent jobEvent) {
        log.info("Job event received - " + jobEvent);

        try {
            switch (jobEvent.getDomainType()) {
                case DIGITAL_FILING:
                    digitalFilingJobProcessor.process(jobEvent);
                    break;

                case TRANSACTIONAL_EMAIL:
                    transactionalEmailJobProcessor.process(jobEvent);
                    break;

                case EMAIL_CAMPAIGN:
                    emailCampaignJobProcessor.process(jobEvent);
                    break;

                case WHATSAPP:
                    whatsAppJobProcessor.process(jobEvent);
                    break;

                case SMS:
                    smsJobProcessor.process(jobEvent);
                    break;

                case MT_WHATSAPP:
                    mtWhatsAppJobProcessor.process(jobEvent);
                    break;

                case MT_TRANSACTIONAL_EMAIL:
                    mtTransactionalEmailJobProcessor.process(jobEvent);
                    break;

                default:
                    log.error("Unsupported job - " + jobEvent);
                    break;
            }

        // KLUDGE: kiasu catch block
        } catch (Exception ex) {
            log.error("Job event processing failed with unknown Server Error - " + jobEvent, ex);

            Optional<Job> jobOptional = jobService.getById(jobEvent.getJobId());
            if (jobOptional.isPresent()) {
                Job targetJob = jobOptional.get();
                targetJob.setStatus(JobStatus.FAILED);
                targetJob.setErrorMessage(ex.getMessage());

                jobService.saveAndFlush(targetJob);
            }
        }
    }

}
