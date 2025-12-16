package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.*;
import com.grabbill.core.service.ActivityTrackingService;
import com.grabbill.core.service.BaseActivityService;
import com.grabbill.core.service.MTWhatsAppRecordService;
import com.grabbill.core.service.TransactionalEmailRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author seez
 */
@Slf4j
public class WhatsAppManagerImpl implements WhatsAppEventManager {
    @Autowired
    private TransactionTemplate txTemplate;

    @Autowired
    @Qualifier("transactionalEmailRecordService")
    TransactionalEmailRecordService transactionalEmailRecordService;

    @Autowired
    @Qualifier("transactionalEmailActivityService")
    private BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> transactionalEmailActivityService;

    @Autowired
    @Qualifier("whatsAppRecordService")
    WhatsAppRecordService whatsAppRecordService;

    @Autowired
    @Qualifier("whatsAppActivityService")
    private BaseActivityService<WhatsAppType, WhatsAppActivity> whatsAppActivityService;

    @Autowired
    @Qualifier("mtWhatsAppRecordService")
    private MTWhatsAppRecordService mtWhatsAppRecordService;

    @Autowired
    @Qualifier("mtWhatsAppActivityService")
    private BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService;

    @Autowired
    private WhatsAppEventService whatsAppEventService;
    
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    @Autowired
    private ActivityTrackingService activityTrackingService;

    @Transactional
    @Override
    public void process() {
        if (isProcessing.get()) {
            log.info("WhatsApp event processing is already in progress (skipping)");
            return;
        }
        isProcessing.set(true);
        try {
            List<WhatsAppEvent> unprocessedEvents = whatsAppEventService.findAllByProcessedIsFalse();

            if (unprocessedEvents.isEmpty()) {
                log.info("No unprocessed whatsapp events");
            } else {
                log.info("Found " + unprocessedEvents.size() + " unprocessed whatsapp events");
            }

            int runProcessedCount = 0;
            txTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
            for (WhatsAppEvent unprocessedEvent : unprocessedEvents) {
                txTemplate.executeWithoutResult(status -> {
                    log.info("Process event for whatsapp message id: " + unprocessedEvent.getWhatsappMessageId());

                    Optional<WhatsAppRecord> whatsAppRecordOptional =
                            whatsAppRecordService.findByWhatsAppMessageId(unprocessedEvent.getWhatsappMessageId());
                    if (whatsAppRecordOptional.isPresent()) {
                        log.warn("WhatsApp record with message id found");
                        WhatsAppRecord whatsAppRecord = whatsAppRecordOptional.get();
                        String whatsappMessageStatus = unprocessedEvent.getStatus();
                        OffsetDateTime whatsAppMessageTimestamp = unprocessedEvent.getEventTimestamp();

                        if (whatsappMessageStatus.equalsIgnoreCase("sent") && !Boolean.TRUE.equals(whatsAppRecord.getWhatsAppStatusSent())) {
                            whatsAppRecord.setWhatsAppStatusSent(true);
                            whatsAppRecord.setWhatsAppStatusSentTimestamp(whatsAppMessageTimestamp);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("delivered") && !Boolean.TRUE.equals(whatsAppRecord.getWhatsAppStatusDelivered())) {
                            whatsAppRecord.setWhatsAppStatusDelivered(true);
                            whatsAppRecord.setWhatsAppStatusDeliveredTimestamp(whatsAppMessageTimestamp);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("read") && !Boolean.TRUE.equals(whatsAppRecord.getWhatsAppStatusRead())) {
                            whatsAppRecord.setWhatsAppStatusRead(true);
                            whatsAppRecord.setWhatsAppStatusReadTimestamp(whatsAppMessageTimestamp);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("failed") && !Boolean.TRUE.equals(whatsAppRecord.getWhatsAppStatusFailed())) {
                            whatsAppRecord.setWhatsAppStatusFailed(true);
                            whatsAppRecord.setWhatsAppStatusFailedTimestamp(whatsAppMessageTimestamp);
                            whatsAppRecord.setWhatsAppStatusFailedMessage(unprocessedEvent.getStatusMessage());
                        }

                        whatsAppRecordService.save(whatsAppRecord);
                        unprocessedEvent.setProcessed(true);
                        whatsAppEventService.save(unprocessedEvent);

                        return;
                    }


                    Optional<MTWhatsAppRecord> mtWhatsAppRecordOptional =
                            mtWhatsAppRecordService.findByWhatsAppMessageId(unprocessedEvent.getWhatsappMessageId());
                    if (mtWhatsAppRecordOptional.isPresent()) {
                        log.warn("Multi-template WhatsApp record with message id found");
                        MTWhatsAppRecord mtWhatsAppRecord = mtWhatsAppRecordOptional.get();
                        MTWhatsAppActivity mtWhatsAppActivity = mtWhatsAppRecord.getMtWhatsAppActivity();
                        String whatsappMessageStatus = unprocessedEvent.getStatus();
                        OffsetDateTime whatsAppMessageTimestamp = unprocessedEvent.getEventTimestamp();

                        if (whatsappMessageStatus.equalsIgnoreCase("sent") && !Boolean.TRUE.equals(mtWhatsAppRecord.getWhatsAppStatusSent())) {
                            mtWhatsAppRecord.setWhatsAppStatusSent(true);
                            mtWhatsAppRecord.setWhatsAppStatusSentTimestamp(whatsAppMessageTimestamp);
                            mtWhatsAppActivity.setWhatsAppStatusSent(mtWhatsAppActivity.getWhatsAppStatusSent() + 1);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("delivered") && !Boolean.TRUE.equals(mtWhatsAppRecord.getWhatsAppStatusDelivered())) {
                            mtWhatsAppRecord.setWhatsAppStatusDelivered(true);
                            mtWhatsAppRecord.setWhatsAppStatusDeliveredTimestamp(whatsAppMessageTimestamp);
                            mtWhatsAppActivity.setWhatsAppStatusDelivered(mtWhatsAppActivity.getWhatsAppStatusDelivered() + 1);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("read") && !Boolean.TRUE.equals(mtWhatsAppRecord.getWhatsAppStatusRead())) {
                            mtWhatsAppRecord.setWhatsAppStatusRead(true);
                            mtWhatsAppRecord.setWhatsAppStatusReadTimestamp(whatsAppMessageTimestamp);
                            mtWhatsAppActivity.setWhatsAppStatusRead(mtWhatsAppActivity.getWhatsAppStatusRead() + 1);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("failed") && !Boolean.TRUE.equals(mtWhatsAppRecord.getWhatsAppStatusFailed())) {
                            mtWhatsAppRecord.setWhatsAppStatusFailed(true);
                            mtWhatsAppRecord.setWhatsAppStatusFailedTimestamp(whatsAppMessageTimestamp);
                            mtWhatsAppRecord.setWhatsAppStatusFailedMessage(unprocessedEvent.getStatusMessage());
                            mtWhatsAppActivity.setWhatsAppStatusFailed(mtWhatsAppActivity.getWhatsAppStatusFailed() + 1);
                        }

                        mtWhatsAppRecordService.save(mtWhatsAppRecord);
                        mtWhatsAppActivityService.save(mtWhatsAppActivity);
                        unprocessedEvent.setProcessed(true);
                        whatsAppEventService.save(unprocessedEvent);
                        activityTrackingService.trackCount(ActivityTrackingService.ActivityType.MT_WHATSAPP, mtWhatsAppActivity.getId());

                        return;
                    }


                    Optional<TransactionalEmailRecord> recordOptional =
                            transactionalEmailRecordService.findByWhatsappMessageId(unprocessedEvent.getWhatsappMessageId());
                    if (recordOptional.isPresent()) {
                        log.warn("Transactional email record with message id found");

                        TransactionalEmailRecord transactionalEmailRecord = recordOptional.get();
                        TransactionalEmailActivity transactionalEmailActivity = transactionalEmailRecord.getTransactionalEmailActivity();
                        String whatsappMessageStatus = unprocessedEvent.getStatus();
                        OffsetDateTime whatsAppMessageTimestamp = unprocessedEvent.getEventTimestamp();

                        if (whatsappMessageStatus.equalsIgnoreCase("sent") && !Boolean.TRUE.equals(transactionalEmailRecord.getWhatsappStatusSent())) {
                            transactionalEmailRecord.setWhatsappStatusSent(true);
                            transactionalEmailRecord.setWhatsappStatusSentTimestamp(whatsAppMessageTimestamp);
                            transactionalEmailActivity.setWhatsAppStatusSent(transactionalEmailActivity.getWhatsAppStatusSent() == null ? 1 : transactionalEmailActivity.getWhatsAppStatusSent() + 1);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("delivered") && !Boolean.TRUE.equals(transactionalEmailRecord.getWhatsappStatusDelivered())) {
                            transactionalEmailRecord.setWhatsappStatusDelivered(true);
                            transactionalEmailRecord.setWhatsappStatusDeliveredTimestamp(whatsAppMessageTimestamp);
                            transactionalEmailActivity.setWhatsAppStatusDelivered(transactionalEmailActivity.getWhatsAppStatusDelivered() == null ? 1 : 1 + transactionalEmailActivity.getWhatsAppStatusDelivered() + 1);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("read") && !Boolean.TRUE.equals(transactionalEmailRecord.getWhatsappStatusRead())) {
                            transactionalEmailRecord.setWhatsappStatusRead(true);
                            transactionalEmailRecord.setWhatsappStatusReadTimestamp(whatsAppMessageTimestamp);
                            transactionalEmailActivity.setWhatsAppStatusRead(transactionalEmailActivity.getWhatsAppStatusRead() == null ? 1 : 1 + transactionalEmailActivity.getWhatsAppStatusRead() + 1);
                        } else if (whatsappMessageStatus.equalsIgnoreCase("failed") && !Boolean.TRUE.equals(transactionalEmailRecord.getWhatsAppStatusFailed())) {
                            transactionalEmailRecord.setWhatsAppStatusFailed(true);
                            transactionalEmailRecord.setWhatsAppStatusFailedTimestamp(whatsAppMessageTimestamp);
                            transactionalEmailRecord.setWhatsAppStatusFailedMessage(unprocessedEvent.getStatusMessage());
                            transactionalEmailActivity.setWhatsAppStatusFailed(transactionalEmailActivity.getWhatsAppStatusFailed() == null ? 1 : 1 + transactionalEmailActivity.getWhatsAppStatusFailed() + 1);
                        }

                        transactionalEmailRecordService.save(transactionalEmailRecord);
                        transactionalEmailActivityService.save(transactionalEmailActivity);
                        unprocessedEvent.setProcessed(true);
                        whatsAppEventService.save(unprocessedEvent);

                        return;
                    }

                    int processedCount = unprocessedEvent.getProcessedCount() == null ? 0 : unprocessedEvent.getProcessedCount();
                    unprocessedEvent.setProcessedCount(++processedCount);
                    whatsAppEventService.save(unprocessedEvent);
                    log.warn("Record not found for whatsapp message id: " + unprocessedEvent.getWhatsappMessageId());
                });

                runProcessedCount++;
                if (runProcessedCount % 500 == 0) {
                    log.info("Processed {} WhatsApp events out of {}", runProcessedCount, unprocessedEvents.size());
                }
            }
        } finally {
            isProcessing.set(false);
        }
    }

    @Override
    public void updateCount() {
        List<MTWhatsAppActivity> allProcessedActivities = mtWhatsAppActivityService.getAllProcessedActivities();
        log.info("Updating processed [{}] activities", allProcessedActivities.size());
        for (MTWhatsAppActivity mtWhatsAppActivity : allProcessedActivities) {
            MTWhatsAppActivity updatedActivity = mtWhatsAppActivityService.updateCount(mtWhatsAppActivity);
            mtWhatsAppActivityService.save(updatedActivity);
            log.info("Updated activity [{}]", updatedActivity.getId());
        }
    }
}
