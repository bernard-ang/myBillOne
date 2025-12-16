package com.grabbill.server.controller;

import com.google.gson.Gson;
import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.WhatsAppEvent;
import com.grabbill.core.model.whatsapp.webhook.*;
import com.grabbill.core.service.AccountService;
import com.grabbill.core.service.TransactionalEmailRecordService;
import com.grabbill.core.service.whatsapp.WhatsAppEventService;
import com.grabbill.core.service.whatsapp.WhatsAppService;
import com.grabbill.core.service.whatsapp.WhatsAppSession;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

/**
 * @author seez
 */
@Slf4j
@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppWebhookController {

    @Autowired
    @Qualifier("transactionalEmailRecordService")
    TransactionalEmailRecordService txerService;

    @Autowired
    private WhatsAppEventService whatsAppEventService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Transactional
    @PostMapping()
    public synchronized ResponseEntity<String> hook(@RequestParam String accountId, @RequestBody String body) {
        log.info("--- Whatsapp webhook: " + accountId);
        log.info(body);

        Account account = accountService.getById(Integer.valueOf(accountId)).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1021,
                        "Account with ID [" + accountId + "] is not found!"
                )
        );

        // TODO: error handling

        Gson gson = new Gson();
        WhatsAppWebhookMessage whatsAppWebhookMessage = gson.fromJson(body, WhatsAppWebhookMessage.class);

        if (whatsAppWebhookMessage.getJson().contains("\"object\":\"whatsapp_business_account\"")) {
            WhatsAppBusinessEvent whatsAppBusinessEvent = gson.fromJson(whatsAppWebhookMessage.getJson(), WhatsAppBusinessEvent.class);
            for (WhatsAppBusinessEntry whatsAppBusinessEntry : whatsAppBusinessEvent.getEntry()) {
                for (WhatsAppBusinessChange change : whatsAppBusinessEntry.getChanges()) {
                    WhatsAppBusinessValue changeValue = change.getValue();
                    if (changeValue.getStatuses() != null) {
                        for (WhatsAppBusinessStatus status : changeValue.getStatuses()) {
                            String whatsappMessageId = status.getId();
                            String whatsappMessageStatus = status.getStatus();
                            String whatsappMessageTimestamp = status.getTimestamp();
                            long epochTime = Long.parseLong(whatsappMessageTimestamp);
                            Instant instant = Instant.ofEpochSecond(epochTime);
                            OffsetDateTime messageTimestamp = OffsetDateTime.ofInstant(instant, ZoneOffset.systemDefault());

                            WhatsAppEvent whatsAppEvent = new WhatsAppEvent();
                            whatsAppEvent.setEventId(whatsAppWebhookMessage.getId());
                            whatsAppEvent.setWhatsappMessageId(whatsappMessageId);
                            whatsAppEvent.setType("business-initiated");
                            whatsAppEvent.setStatus(whatsappMessageStatus);
                            whatsAppEvent.setEventTimestamp(messageTimestamp);
                            whatsAppEvent.setAccount(account);
                            whatsAppEvent.setMobileNo(status.getRecipient_id());
                            whatsAppEvent.setProcessed(false);

                            if (status.getErrors() != null && !status.getErrors().isEmpty()) {
                                whatsAppEvent.setStatusMessage(
                                        status.getErrors().stream()
                                                .map(whatsAppError -> whatsAppError.getMessage() + ". " + whatsAppError.getError_data().getDetails())
                                                .collect(Collectors.joining(","))
                                );
                            }

                            whatsAppEventService.save(whatsAppEvent);
                        }
                    } else if (changeValue.getMessages() != null) {
                        for (WhatsAppBusinessMessage message : changeValue.getMessages()) {
                            String whatsappMessageId = message.getId();
                            String whatsappMessageTimestamp = message.getTimestamp();
                            long epochTime = Long.parseLong(whatsappMessageTimestamp);
                            Instant instant = Instant.ofEpochSecond(epochTime);
                            OffsetDateTime messageTimestamp = OffsetDateTime.ofInstant(instant, ZoneOffset.systemDefault());
                            String messageType = message.getType();

                            WhatsAppEvent whatsAppEvent = new WhatsAppEvent();
                            whatsAppEvent.setEventId(whatsAppWebhookMessage.getId());
                            whatsAppEvent.setWhatsappMessageId(whatsappMessageId);
                            whatsAppEvent.setType("user-initiated");
                            whatsAppEvent.setMessageType(messageType);
                            whatsAppEvent.setContent(messageType.equals("text") ? message.getText().getBody() : "Message is type of " + messageType);
                            whatsAppEvent.setEventTimestamp(messageTimestamp);
                            whatsAppEvent.setAccount(account);
                            whatsAppEvent.setMobileNo(message.getFrom());
                            whatsAppEvent.setProcessed(true);

                            whatsAppEventService.save(whatsAppEvent);

                            if (account.getWabaAutoReplyMessage() != null && !account.getWabaAutoReplyMessage().trim().isEmpty()) {
                                WhatsAppSession session = whatsAppService.login(account.getWabaGuid(), account.getWabaEmail(), account.getWabaPassword());
                                session.sendTextMessage(message.getFrom(), account.getWabaAutoReplyMessage());
                            }
                        }
                    }
                }
            }
        }

        return ResponseEntity.ok("ok");
    }

}
