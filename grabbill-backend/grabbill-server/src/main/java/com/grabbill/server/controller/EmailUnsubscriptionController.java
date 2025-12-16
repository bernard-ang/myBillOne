package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.BaseActivityService;
import com.grabbill.core.service.UnsubscribedEmailLinkService;
import com.grabbill.core.service.UnsubscribedEmailService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.UnsubscribeEmailRequest;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.UnsubscribedEmailPayload;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/unsubscribe")
public class EmailUnsubscriptionController {

    @Autowired
    private UnsubscribedEmailService unsubscribedEmailService;

    @Autowired
    private UnsubscribedEmailLinkService unsubscribedEmailLinkService;

    @Autowired
    @Qualifier("transactionalEmailActivityService")
    private BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> txeaService;

    @Autowired
    @Qualifier("mtTransactionalEmailActivityService")
    private BaseActivityService<MTTransactionalEmailType, MTTransactionalEmailActivity> mtTxeaService;

    @Autowired
    @Qualifier("emailCampaignActivityService")
    private BaseActivityService<EmailCampaignType, EmailCampaignActivity> ecaService;


    @Transactional
    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> unsubscribe(
            @RequestParam String linkId,
            @RequestBody UnsubscribeEmailRequest request
    ) {
        UnsubscribedEmailLink targetLink = unsubscribedEmailLinkService.getByLinkId(linkId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1007,
                        "Invalid unsubscription link"
                )
        );


        UnsubscribedEmailPayload response = new UnsubscribedEmailPayload();
        if (DomainType.TRANSACTIONAL_EMAIL.equals(targetLink.getDomainType())) {
            TransactionalEmailActivity targetActivity = txeaService.getById(targetLink.getActivityId()).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB0999,
                            "Invalid unsubscription link with broken reference"
                    )
            );
            TransactionalEmailType targetType = targetActivity.getTransactionalEmailType();
            Account account = targetType.getAccount();

            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (targetLink.getUnsubscribedDate() == null) {
                targetLink.setUnsubscribedDate(now);
                unsubscribedEmailLinkService.save(targetLink);

                boolean recordUpdated = false;
                for (TransactionalEmailIndexRow indexRow : targetActivity.getTransactionalEmailIndexRows()) {
                    if (indexRow.getText1().equals(targetLink.getEmail())) {
                        TransactionalEmailRecord record = indexRow.getTransactionalEmailRecord();
                        record.setEmailStatusUnsubscribedTimestamp(now);
                        record.setEmailStatusUnsubscribedReason(request.getReason());
                        targetActivity.setEmailStatusUnsubscribedSkip(targetActivity.getEmailStatusUnsubscribedSkip());

                        recordUpdated = true;

                        break;
                    }
                }
                if (recordUpdated) {
                    targetActivity = txeaService.saveAndFlush(targetActivity);
                }

            } else {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1026,
                        "already unsubscribed at " + targetLink.getUnsubscribedDate().toString()
                );
            }

            UnsubscribedEmail instance;
            if (!unsubscribedEmailService.isUnsubscribed(
                    account.getId(),
                    targetLink.getEmail(),
                    targetLink.getDomainType(),
                    targetLink.getTypeId()
            )) {
                instance = new UnsubscribedEmail();
                instance.setAccount(account);
                instance.setTypeId(targetType.getId());
                instance.setDomainType(DomainType.TRANSACTIONAL_EMAIL);
                instance.setTypeId(targetType.getId());
                instance.setTypeName(targetType.getName());
                instance.setActivityId(targetActivity.getId());
                instance.setActivityName(targetActivity.getName());
                instance.setEmail(targetLink.getEmail());
                instance.setReason(request.getReason());
                instance = unsubscribedEmailService.save(instance);

            } else {
                instance = unsubscribedEmailService.get(
                        account.getId(),
                        targetLink.getEmail(),
                        targetLink.getDomainType(),
                        targetLink.getTypeId()
                ).get(0);
            }

            response = UnsubscribedEmailPayload.from(instance);
            // TODO: log the action


        } else if (DomainType.MT_TRANSACTIONAL_EMAIL.equals(targetLink.getDomainType())) {
            MTTransactionalEmailActivity targetActivity = mtTxeaService.getById(targetLink.getActivityId()).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB0999,
                            "Invalid unsubscription link with broken reference"
                    )
            );
            MTTransactionalEmailType targetType = targetActivity.getMtTransactionalEmailType();
            Account account = targetType.getAccount();

            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (targetLink.getUnsubscribedDate() == null) {
                targetLink.setUnsubscribedDate(now);
                unsubscribedEmailLinkService.save(targetLink);

                boolean recordUpdated = false;
                for (MTTransactionalEmailIndexRow indexRow : targetActivity.getMtTransactionalEmailIndexRows()) {
                    if (indexRow.getText1().equals(targetLink.getEmail())) {
                        MTTransactionalEmailRecord record = indexRow.getMtTransactionalEmailRecord();
                        record.setEmailStatusUnsubscribedTimestamp(now);
                        record.setEmailStatusUnsubscribedReason(request.getReason());
                        targetActivity.setEmailStatusUnsubscribedSkip(targetActivity.getEmailStatusUnsubscribedSkip());

                        recordUpdated = true;

                        break;
                    }
                }
                if (recordUpdated) {
                    targetActivity = mtTxeaService.saveAndFlush(targetActivity);
                }

            } else {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1026,
                        "already unsubscribed at " + targetLink.getUnsubscribedDate().toString()
                );
            }

            UnsubscribedEmail instance;
            if (!unsubscribedEmailService.isUnsubscribed(
                    account.getId(),
                    targetLink.getEmail(),
                    targetLink.getDomainType(),
                    targetLink.getTypeId()
            )) {
                instance = new UnsubscribedEmail();
                instance.setAccount(account);
                instance.setTypeId(targetType.getId());
                instance.setDomainType(DomainType.MT_TRANSACTIONAL_EMAIL);
                instance.setTypeId(targetType.getId());
                instance.setTypeName(targetType.getName());
                instance.setActivityId(targetActivity.getId());
                instance.setActivityName(targetActivity.getName());
                instance.setEmail(targetLink.getEmail());
                instance.setReason(request.getReason());
                instance = unsubscribedEmailService.save(instance);

            } else {
                instance = unsubscribedEmailService.get(
                        account.getId(),
                        targetLink.getEmail(),
                        targetLink.getDomainType(),
                        targetLink.getTypeId()
                ).get(0);
            }

            response = UnsubscribedEmailPayload.from(instance);
            // TODO: log the action


        } else if (DomainType.EMAIL_CAMPAIGN.equals(targetLink.getDomainType())) {
            EmailCampaignActivity targetActivity = ecaService.getById(targetLink.getActivityId()).orElseThrow(
                    () -> new GrabbillServerException(
                            GrabbillServerErrorCode.GRB0999,
                            "Invalid unsubscription link with broken reference"
                    )
            );
            EmailCampaignType targetType = targetActivity.getEmailCampaignType();
            Account account = targetType.getAccount();

            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (targetLink.getUnsubscribedDate() == null) {
                targetLink.setUnsubscribedDate(now);
                unsubscribedEmailLinkService.save(targetLink);

                boolean recordUpdated = false;
                for (EmailCampaignIndexRow indexRow : targetActivity.getEmailCampaignIndexRows()) {
                    if (indexRow.getText1().equals(targetLink.getEmail())) {
                        EmailCampaignRecord record = indexRow.getEmailCampaignRecord();
                        record.setEmailStatusUnsubscribedTimestamp(now);
                        record.setEmailStatusUnsubscribedReason(request.getReason());
                        targetActivity.setEmailStatusUnsubscribedSkip(targetActivity.getEmailStatusUnsubscribedSkip());

                        recordUpdated = true;

                        break;
                    }
                }
                if (recordUpdated) {
                    targetActivity = ecaService.saveAndFlush(targetActivity);
                }

            } else {
                throw new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1026,
                        "already unsubscribed at " + targetLink.getUnsubscribedDate().toString()
                );
            }

            UnsubscribedEmail instance;
            if (!unsubscribedEmailService.isUnsubscribed(
                    account.getId(),
                    targetLink.getEmail(),
                    targetLink.getDomainType(),
                    targetLink.getTypeId()
            )) {
                instance = new UnsubscribedEmail();
                instance.setAccount(account);
                instance.setTypeId(targetType.getId());
                instance.setDomainType(DomainType.EMAIL_CAMPAIGN);
                instance.setTypeId(targetType.getId());
                instance.setTypeName(targetType.getName());
                instance.setActivityId(targetActivity.getId());
                instance.setActivityName(targetActivity.getName());
                instance.setEmail(targetLink.getEmail());
                instance.setReason(request.getReason());
                instance = unsubscribedEmailService.save(instance);

            } else {
                instance = unsubscribedEmailService.get(
                        account.getId(),
                        targetLink.getEmail(),
                        targetLink.getDomainType(),
                        targetLink.getTypeId()
                ).get(0);
            }

            response = UnsubscribedEmailPayload.from(instance);

        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        response
                )
        );
    }

}
