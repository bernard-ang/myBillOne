package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.service.BaseActivityService;
import com.grabbill.core.service.MTWhatsAppRecordService;
import com.grabbill.core.service.TransactionalEmailRecordService;
import com.grabbill.core.service.whatsapp.WhatsAppRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * @author seez
 */
@Slf4j
@RestController
@RequestMapping("/ack/whatsapp")
public class WhatsappAcknowledgementController {

    @Autowired
    @Qualifier("transactionalEmailRecordService")
    private TransactionalEmailRecordService txerService;

    @Autowired
    @Qualifier("transactionalEmailActivityService")
    private BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> transactionalEmailActivityService;

    @Autowired
    @Qualifier("whatsAppRecordService")
    private WhatsAppRecordService whatsAppRecordService;

    @Autowired
    @Qualifier("whatsAppActivityService")
    private BaseActivityService<WhatsAppType, WhatsAppActivity> whatsAppActivityService;

    @Autowired
    @Qualifier("mtWhatsAppRecordService")
    private MTWhatsAppRecordService mtWhatsAppRecordService;

    @Autowired
    @Qualifier("mtWhatsAppActivityService")
    private BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> mtWhatsAppActivityService;

    @Value("${whatsapp.ack.redirect.url}")
    private String redirectUrl;

    @Transactional
    @GetMapping()
    public RedirectView hook(@RequestParam String recordId) {
        String[] values = recordId.split("-");
        String domain = values[0];
        recordId = values[1];
        log.info("--- ack: " + domain);

        if (domain == null) {
            return new RedirectView(redirectUrl + "?status=failed");

        // transactional email
        } else if (domain.equalsIgnoreCase("tx")) {
            Optional<TransactionalEmailRecord> recordOptional = txerService.findById(Long.valueOf(recordId));
            if (recordOptional.isEmpty()) {
                return new RedirectView(redirectUrl + "?status=failed");
            }

            TransactionalEmailRecord transactionalEmailRecord = recordOptional.get();
            if (transactionalEmailRecord.getWhatsappStatusAcknowledge() == null || !transactionalEmailRecord.getWhatsappStatusAcknowledge()) {
                transactionalEmailRecord.setWhatsappStatusAcknowledge(true);
                transactionalEmailRecord.setWhatsappStatusAcknowledgeTimestamp(OffsetDateTime.now());

                TransactionalEmailActivity transactionalEmailActivity = transactionalEmailRecord.getTransactionalEmailActivity();
                transactionalEmailActivity.setWhatsAppStatusAcknowledge(transactionalEmailActivity.getWhatsAppStatusAcknowledge() + 1);

                txerService.save(transactionalEmailRecord);
                transactionalEmailActivityService.save(transactionalEmailActivity);
            }
            return new RedirectView(redirectUrl + "?status=success");

        // multi-template whatsapp
        } else if (domain.equalsIgnoreCase("mtwa")) {
            Optional<MTWhatsAppRecord> recordOptional = mtWhatsAppRecordService.findById(Long.valueOf(recordId));
            if (recordOptional.isEmpty()) {
                return new RedirectView(redirectUrl + "?status=failed");
            }

            MTWhatsAppRecord record = recordOptional.get();
            if (record.getWhatsAppStatusAcknowledge() == null || !record.getWhatsAppStatusAcknowledge()) {
                record.setWhatsAppStatusAcknowledge(true);
                record.setWhatsAppStatusAcknowledgeTimestamp(OffsetDateTime.now());

                MTWhatsAppActivity activity = record.getMtWhatsAppActivity();
                activity.setWhatsAppStatusAcknowledge(activity.getWhatsAppStatusAcknowledge() + 1);

                mtWhatsAppRecordService.save(record);
                mtWhatsAppActivityService.save(activity);
            }
            return new RedirectView(redirectUrl + "?status=success");

        // whatsapp
        } else {
            Optional<WhatsAppRecord> recordOptional = whatsAppRecordService.findById(Long.valueOf(recordId));
            if (recordOptional.isEmpty()) {
                return new RedirectView(redirectUrl + "?status=failed");
            }

            WhatsAppRecord record = recordOptional.get();
            if (record.getWhatsAppStatusAcknowledge() == null || !record.getWhatsAppStatusAcknowledge()) {
                record.setWhatsAppStatusAcknowledge(true);
                record.setWhatsAppStatusAcknowledgeTimestamp(OffsetDateTime.now());

                WhatsAppActivity activity = record.getWhatsAppActivity();
                activity.setWhatsAppStatusAcknowledge(activity.getWhatsAppStatusAcknowledge() + 1);

                whatsAppRecordService.save(record);
                whatsAppActivityService.save(activity);
            }
            return new RedirectView(redirectUrl + "?status=success");
        }
    }
}
