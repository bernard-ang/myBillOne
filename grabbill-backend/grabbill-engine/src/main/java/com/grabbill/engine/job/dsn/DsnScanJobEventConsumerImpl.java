package com.grabbill.engine.job.dsn;

import com.grabbill.core.model.job.event.DsnScanJobEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * @author michaellow
 */
@Slf4j
public class DsnScanJobEventConsumerImpl implements DsnScanJobEventConsumer {

    @Autowired
    private DsnScanJobProcessor dsnScanJobProcessor;


    @RabbitListener(
            queuesToDeclare = {
                    @Queue(
                            name = "${dsn-scan-job.queue.name}",
                            durable = "true"
                    )
            },
            concurrency = "${dsn-scan-job.rabbitmq.listener.simple.concurrency}-${dsn-scan-job.rabbitmq.listener.simple.max-concurrency}"
    )
    @Override
    public void consume(@Payload final DsnScanJobEvent targetJobEvent) {
        log.debug("DSN job event received: " + targetJobEvent.toString());
        dsnScanJobProcessor.process(targetJobEvent);
    }

}
