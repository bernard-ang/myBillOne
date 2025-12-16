package com.grabbill.server.configuration;

import com.grabbill.core.service.whatsapp.WhatsAppEventManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author seez
 */
@EnableScheduling
@Slf4j
public class WhatsappEventConfiguration {

    @Autowired
    private WhatsAppEventManager whatsAppEventManager;

    @Scheduled(
            cron = "${whatsapp-event.process-interval.cron}",
            zone = "${whatsapp-event.process-interval.timezone}"
    )
    public void run() {
        log.debug("Event starts processing.");
        whatsAppEventManager.process();
    }

}
