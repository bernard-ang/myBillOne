package com.grabbill.core.service;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppType;
import com.grabbill.core.model.ProcessStatus;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

/**
 * A thread-safe, in-memory activity tracking service.
 */
@Slf4j
public class ActivityTrackingServiceImpl implements ActivityTrackingService {
    public ActivityTrackingServiceImpl(BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService, TransactionTemplate txTemplate) {
        this.mtWhatsAppActivityService = mtWhatsAppActivityService;
        this.txTemplate = txTemplate;
    }

    private final Set<TrackedActivity> completedActivities = new CopyOnWriteArraySet<>();
    private final Set<TrackedActivity> countActivities = new CopyOnWriteArraySet<>();

    private final BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService;
    private final TransactionTemplate txTemplate;

    @PostConstruct
    public void init() {
        log.info("Activity tracking service initialized");
    }

    @Override
    public void trackCompleted(ActivityType activityType, Long id) {
        completedActivities.add(new TrackedActivity(activityType, id));
    }

    @Override
    public void trackCount(ActivityType activityType, Long id) {
        countActivities.add(new TrackedActivity(activityType, id));
    }

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void process() {
        for (TrackedActivity completed : completedActivities) {
            if (ActivityType.MT_WHATSAPP.equals(completed.activityType)) {
                if (processCompletedMtWhatsapp(completed.id)) {
                    completedActivities.remove(completed);
                }
            } else {
                log.warn("Unexpected activity type: {}", completed.getActivityType());
                completedActivities.remove(completed);
            }
        }

        for (TrackedActivity countActivity : countActivities) {
            if (ActivityType.MT_WHATSAPP.equals(countActivity.activityType)) {
                processCountMtWhatsapp(countActivity.id);
            } else {
                log.warn("Unexpected activity type: {}", countActivity.getActivityType());
            }
            countActivities.remove(countActivity);
        }
    }

    /**
     * Verify completed activity state.
     *
     * @param id
     * @return true if state is verified, false to retry
     */
    private boolean processCompletedMtWhatsapp(Long id) {
        long startTime = System.currentTimeMillis();

        AtomicBoolean processSuccess = new AtomicBoolean(true);

        mtWhatsAppActivityService.getById(id).ifPresentOrElse(
                activity -> {
                    if (ProcessStatus.COMPLETED.equals(activity.getStatus())) {
                        log.info("MT WA activity {} completed status verified", id);
                    } else {
                        log.warn("MT WA activity {} completed status verification require post-processing", id);
                        activity.setStatus(ProcessStatus.COMPLETED);
                        activity.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                        // fail-safe mechanism - retry this
                        processSuccess.set(false);
                    }
                    mtWhatsAppActivityService.updateCount(activity);
                    txTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    txTemplate.executeWithoutResult(status -> {
                        mtWhatsAppActivityService.save(activity);
                    });
                },
                () -> log.warn("MT WA activity not found: {}", id)
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("MT WA activity {} completed status verification took {}ms", id, duration);

        return processSuccess.get();
    }

    private void processCountMtWhatsapp(Long id) {
        long startTime = System.currentTimeMillis();

        mtWhatsAppActivityService.getById(id).ifPresentOrElse(
                activity -> {
                    mtWhatsAppActivityService.updateCount(activity);
                    txTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
                    txTemplate.executeWithoutResult(status -> {
                        mtWhatsAppActivityService.save(activity);
                    });
                    log.info("MT WA activity {} count updated", id);

                },
                () -> log.warn("MT WA activity not found: {}", id)
        );

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("MT WA activity {} count update took {}ms", id, duration);
    }

    @Value
    static class TrackedActivity {
        ActivityType activityType;
        Long id;
    }
}
