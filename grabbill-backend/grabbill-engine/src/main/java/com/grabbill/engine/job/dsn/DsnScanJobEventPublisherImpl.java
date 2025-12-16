package com.grabbill.engine.job.dsn;

import com.grabbill.core.model.job.event.DsnScanJobEvent;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Default {@link DsnScanJobEventPublisher} implementation.
 *
 * @author michaellow
 */
public class DsnScanJobEventPublisherImpl implements DsnScanJobEventPublisher {

    @Autowired
    @Qualifier("dsnScanJobQueue")
    private Queue jobQueue;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Override
    public void publish(final DsnScanJobEvent dsnScanJobEvent) {
        rabbitTemplate.convertAndSend(jobQueue.getName(), dsnScanJobEvent);
    }

}
