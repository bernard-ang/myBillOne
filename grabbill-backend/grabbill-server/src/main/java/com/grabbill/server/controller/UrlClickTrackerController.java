package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.BaseIndexRowService;
import com.grabbill.core.service.EmbeddedLinkClickService;
import com.grabbill.core.service.EmbeddedLinkService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Objects;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/clicks")
public class UrlClickTrackerController {

    @Autowired
    @Qualifier("transactionalEmailIndexRowService")
    private BaseIndexRowService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailIndexRow> txeirService;

    @Autowired
    @Qualifier("mtTransactionalEmailIndexRowService")
    private BaseIndexRowService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailIndexRow> mtTxeirService;

    @Autowired
    @Qualifier("emailCampaignIndexRowService")
    private BaseIndexRowService<EmailCampaignType, EmailCampaignActivity, EmailCampaignIndexRow> ecirService;

    @Autowired
    private EmbeddedLinkService embeddedLinkService;

    @Autowired
    private EmbeddedLinkClickService embeddedLinkClickService;


    @Transactional
    @GetMapping(path = "/{base64Path}")
    public RedirectView trackUrlClick(@PathVariable String base64Path) {
        String path = new String(Base64.getDecoder().decode(base64Path));

        if (path.startsWith("trxemail")) {
            String[] pathVariables = path.split("/");
            long actId = Long.valueOf(pathVariables[1]);
            long urlId = Long.valueOf(pathVariables[3]);
            long rowId = Long.valueOf(pathVariables[5]);

            return trackTransactionalEmailUrlClick(actId, urlId, rowId);

        } else if (path.startsWith("mt-trxemail")) {
            String[] pathVariables = path.split("/");
            long actId = Long.valueOf(pathVariables[1]);
            long urlId = Long.valueOf(pathVariables[3]);
            long rowId = Long.valueOf(pathVariables[5]);

            return trackMtTransactionalEmailUrlClick(actId, urlId, rowId);

        } else if (path.startsWith("ecemail")) {
            String[] pathVariables = path.split("/");
            long actId = Long.valueOf(pathVariables[1]);
            long urlId = Long.valueOf(pathVariables[3]);
            long rowId = Long.valueOf(pathVariables[5]);

            return trackEmailCampaignUrlClick(actId, urlId, rowId);

        } else {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1030,
                    "Unable to process embedded link [" + base64Path + "]!"
            );
        }
    }


    private RedirectView trackTransactionalEmailUrlClick(
            final Long actId,
            final Long urlId,
            final Long rowId
    ) {
        TransactionalEmailIndexRow targetIndexRow = txeirService.getById(rowId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3012,
                        "Invalid transaction email index row id"
                )
        );
        EmbeddedLink embeddedLink = embeddedLinkService.getByIdAndDomainType(urlId, DomainType.TRANSACTIONAL_EMAIL).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1030,
                        "Embedded link with id [" + urlId + "] of transactional email type is not found!"
                )
        );

        TransactionalEmailActivity targetActivity = targetIndexRow.getTransactionalEmailActivity();
        if (!Objects.equals(targetActivity.getId(), actId)) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3003,
                    "Invalid transaction email activity id"
            );

        } else if (!Objects.equals(targetActivity.getId(), embeddedLink.getActivityId())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1030,
                    "Embedded link of transactional email type does not match activity id of [" + actId + "]!"
            );
        }


        EmbeddedLinkClick embeddedLinkClick = new EmbeddedLinkClick();
        embeddedLinkClick.setEmail(targetIndexRow.getText1());
        embeddedLinkClick.setEmbeddedLink(embeddedLink);
        embeddedLinkClick.setClickedDate(OffsetDateTime.now(ZoneOffset.UTC));
        embeddedLinkClickService.save(embeddedLinkClick);

        return new RedirectView(embeddedLink.getUrl());
    }

    private RedirectView trackMtTransactionalEmailUrlClick(
            final Long actId,
            final Long urlId,
            final Long rowId
    ) {
        MTTransactionalEmailIndexRow targetIndexRow = mtTxeirService.getById(rowId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3012,
                        "Invalid multi-template transaction email index row id"
                )
        );
        EmbeddedLink embeddedLink = embeddedLinkService.getByIdAndDomainType(urlId, DomainType.MT_TRANSACTIONAL_EMAIL).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1030,
                        "Embedded link with id [" + urlId + "] of multi-template transactional email type is not found!"
                )
        );

        MTTransactionalEmailActivity targetActivity = targetIndexRow.getMtTransactionalEmailActivity();
        if (!Objects.equals(targetActivity.getId(), actId)) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB3003,
                    "Invalid multi-template transaction email activity id"
            );

        } else if (!Objects.equals(targetActivity.getId(), embeddedLink.getActivityId())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1030,
                    "Embedded link of multi-template transactional email type does not match activity id of [" + actId + "]!"
            );
        }


        EmbeddedLinkClick embeddedLinkClick = new EmbeddedLinkClick();
        embeddedLinkClick.setEmail(targetIndexRow.getText1());
        embeddedLinkClick.setEmbeddedLink(embeddedLink);
        embeddedLinkClick.setClickedDate(OffsetDateTime.now(ZoneOffset.UTC));
        embeddedLinkClickService.save(embeddedLinkClick);

        return new RedirectView(embeddedLink.getUrl());
    }

    private RedirectView trackEmailCampaignUrlClick(
            final Long actId,
            final Long urlId,
            final Long rowId
    ) {
        EmailCampaignIndexRow targetIndexRow = ecirService.getById(rowId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB4009,
                        "Invalid email campaign index row id"
                )
        );
        EmbeddedLink embeddedLink = embeddedLinkService.getByIdAndDomainType(urlId, DomainType.EMAIL_CAMPAIGN).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1030,
                        "Embedded link with id [" + urlId + "] of email campaign type is not found!"
                )
        );

        EmailCampaignActivity targetActivity = targetIndexRow.getEmailCampaignActivity();
        if (!Objects.equals(targetActivity.getId(), actId)) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB4003,
                    "Invalid email campaign activity id"
            );

        } else if (!Objects.equals(targetActivity.getId(), embeddedLink.getActivityId())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1030,
                    "Embedded link of email campaign type does not match activity id of [" + actId + "]!"
            );
        }


        EmbeddedLinkClick embeddedLinkClick = new EmbeddedLinkClick();
        embeddedLinkClick.setEmail(targetIndexRow.getText1());
        embeddedLinkClick.setEmbeddedLink(embeddedLink);
        embeddedLinkClick.setClickedDate(OffsetDateTime.now(ZoneOffset.UTC));
        embeddedLinkClickService.save(embeddedLinkClick);

        return new RedirectView(embeddedLink.getUrl());
    }

}
