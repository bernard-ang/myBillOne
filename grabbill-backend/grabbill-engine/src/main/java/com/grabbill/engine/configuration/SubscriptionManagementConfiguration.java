package com.grabbill.engine.configuration;

import com.grabbill.engine.service.SubscriptionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author michaellow
 */
@EnableScheduling
@Slf4j
public class SubscriptionManagementConfiguration {

    @Autowired
    private SubscriptionManager subscriptionManager;


    @Scheduled(
            cron = "${subscription-manager.process-interval.cron}",
            zone = "${subscription-manager.process-interval.timezone}"
    )
    public void run() {
        log.info("Subscription Manager starts processing.");
        subscriptionManager.process();
    }

}
