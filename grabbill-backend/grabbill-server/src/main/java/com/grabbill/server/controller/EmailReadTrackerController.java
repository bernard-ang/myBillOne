package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.service.BaseIndexRowService;
import com.grabbill.core.service.BaseRecordService;
import com.grabbill.core.service.MTTransactionalEmailRecordService;
import com.grabbill.core.service.TransactionalEmailRecordService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/track")
public class EmailReadTrackerController {

    private static final String PIXEL_PNG = "pixel.png";

    @Autowired
    @Qualifier("transactionalEmailIndexRowService")
    private BaseIndexRowService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailIndexRow> txeirService;

    @Autowired
    @Qualifier("transactionalEmailRecordService")
    TransactionalEmailRecordService txerService;

    @Autowired
    @Qualifier("mtTransactionalEmailIndexRowService")
    private BaseIndexRowService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailIndexRow> mtTxeirService;

    @Autowired
    @Qualifier("mtTransactionalEmailRecordService")
    private MTTransactionalEmailRecordService mtTxerService;

    @Autowired
    @Qualifier("emailCampaignIndexRowService")
    private BaseIndexRowService<EmailCampaignType, EmailCampaignActivity, EmailCampaignIndexRow> ecirService;

    @Autowired
    @Qualifier("emailCampaignRecordService")
    private BaseRecordService<EmailCampaignRecord> ecrService;


    @Transactional
    @GetMapping(path = "/trxemail/{id}/" + PIXEL_PNG)
    public ResponseEntity<ByteArrayResource> trackTransactionalEmail(@PathVariable Long id) throws IOException {

        TransactionalEmailIndexRow targetIndexRow = txeirService.getById(id).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3012,
                        "Invalid transaction email index row id"
                )
        );
        TransactionalEmailRecord targetRecord = targetIndexRow.getTransactionalEmailRecord();

        // update open if email is unread
        if (targetRecord.getEmailStatusUserReadTimestamp() == null) {
            targetRecord.setEmailStatusUserReadTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            TransactionalEmailActivity targetActivity = targetIndexRow.getTransactionalEmailActivity();
            targetActivity.setEmailStatusOpened(targetActivity.getEmailStatusOpened() + 1);

            txeirService.save(targetIndexRow);
        }

        return loadEmptyPixel();
    }

    @Transactional
    @GetMapping(path = "/mt-trxemail/{id}/" + PIXEL_PNG)
    public ResponseEntity<ByteArrayResource> trackMultiTemplateTransactionalEmail(@PathVariable Long id) throws IOException {

        MTTransactionalEmailIndexRow targetIndexRow = mtTxeirService.getById(id).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB3012,
                        "Invalid multi-template transaction email index row id"
                )
        );
        MTTransactionalEmailRecord targetRecord = targetIndexRow.getMtTransactionalEmailRecord();

        // update open if email is unread
        if (targetRecord.getEmailStatusUserReadTimestamp() == null) {
            targetRecord.setEmailStatusUserReadTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            MTTransactionalEmailActivity targetActivity = targetIndexRow.getMtTransactionalEmailActivity();
            targetActivity.setEmailStatusOpened(targetActivity.getEmailStatusOpened() + 1);

            mtTxeirService.save(targetIndexRow);
        }

        return loadEmptyPixel();
    }

    @Transactional
    @GetMapping(path = "/ecemail/{id}/" + PIXEL_PNG)
    public ResponseEntity<ByteArrayResource> trackEmailCampaignEmail(@PathVariable Long id) throws IOException {

        EmailCampaignIndexRow targetIndexRow = ecirService.getById(id).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB4009,
                        "Invalid email campaign index row id"
                )
        );
        EmailCampaignRecord targetRecord = targetIndexRow.getEmailCampaignRecord();

        // update open if email is unread
        if (targetRecord.getEmailStatusUserReadTimestamp() == null) {
            targetRecord.setEmailStatusUserReadTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
            EmailCampaignActivity targetActivity = targetIndexRow.getEmailCampaignActivity();
            targetActivity.setEmailStatusOpened(targetActivity.getEmailStatusOpened() + 1);

            ecirService.save(targetIndexRow);
        }

        return loadEmptyPixel();
    }

    private ResponseEntity<ByteArrayResource> loadEmptyPixel() throws IOException {

        ClassPathResource classPathResource = new ClassPathResource("static/" + PIXEL_PNG, this.getClass().getClassLoader());
        byte[] bytes = classPathResource.getInputStream().readAllBytes();

        HttpHeaders headers = new HttpHeaders();
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.IMAGE_PNG);
//        headers.setContentDisposition(ContentDisposition.attachment().filename(PIXEL_PNG).build());

        return new ResponseEntity<>(new ByteArrayResource(bytes), headers, HttpStatus.OK);
    }

}
