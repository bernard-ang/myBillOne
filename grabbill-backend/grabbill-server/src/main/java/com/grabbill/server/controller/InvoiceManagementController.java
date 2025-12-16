package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.model.StripeEventType;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.core.service.AccountService;
import com.grabbill.core.service.InvoiceService;
import com.grabbill.core.service.PromoCodeService;
import com.grabbill.core.service.payment.StripeEventService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AccountSubscriptionPayload;
import com.grabbill.server.controller.response.payload.InvoiceBasicPayload;
import com.grabbill.server.controller.response.payload.InvoicePayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.InvoiceReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/mgmt/invoices")
public class InvoiceManagementController extends BaseManagementController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private StripeEventService stripeEventService;

    @Autowired
    private InvoiceReportService invoiceReportService;

    @Autowired
    private PromoCodeService promoCodeService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getInvoices(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) String invoiceNo,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        checkStatus(userDetails);

        Page<Invoice> page = invoiceService.searchInvoices(accountName, invoiceNo, planName, status, startDate, endDate, pageable);

        SearchResultPayload<InvoiceBasicPayload> searchResultPayload =
                SearchResultPayload.<InvoiceBasicPayload>builder()
                        .items(page.get()
                                .map(InvoiceBasicPayload::from)
                                .collect(Collectors.toList())
                        )
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }


    @Transactional
    @GetMapping(value = "/{invoiceId}")
    public ResponseEntity<GrabbillApiResponse> getInvoice(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Integer invoiceId
    ) {
        checkStatus(userDetails);

        Invoice invoice = invoiceService.getById(invoiceId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB8101,
                        "Invoice with ID [" + invoiceId + "] is not found!"
                )
        );
        List<StripeEvent> stripeEvents = stripeEventService.getByTypeAndRefId(
                StripeEventType.INVOICE, invoice.getStripeInvoiceId());

        AccountSubscription accountSubscription = invoice.getAccountSubscription();
        double totalAmountBeforeDiscount = (invoice.getStoragePrice() + invoice.getEmailCampaignPrice()
                + invoice.getTransactionalEmailPrice() + invoice.getSmsTopupPrice()) *
                (SubscriptionMode.ANNUALLY.equals(accountSubscription.getMode()) ? 12 : 1);


        PromoCode targetPromoCode = null;
        String promoCode = accountSubscription.getPromoCode();
        if (promoCode != null) {
            Optional<PromoCode> promoCodeOptional = promoCodeService.getByCode(promoCode);
            if (promoCodeOptional.isPresent()) {
                targetPromoCode = promoCodeOptional.get();
            }
        }


        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        InvoicePayload.from(
                                invoice,
                                stripeEvents,
                                targetPromoCode,
                                totalAmountBeforeDiscount,
                                AccountSubscriptionPayload.from(accountSubscription)
                        )
                )
        );
    }

    @Transactional
    @GetMapping(value = "/reports")
    public ResponseEntity<ByteArrayResource> downloadReport(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam (required = false) String account,
            @RequestParam (required = false) String affiliateCode,
            @RequestParam String tz
    ) {
        checkStatus(userDetails);

        List<Account> accounts = new ArrayList<>();

        if (StringUtils.hasLength(account) && StringUtils.hasLength(affiliateCode)) {
            accounts.addAll(accountService.getByCompanyNameAndAffiliateMasterCode(account, affiliateCode));

        } else if (StringUtils.hasLength(account)) {
            accounts.addAll(accountService.getByCompanyName(account));

        } else if (StringUtils.hasLength(affiliateCode)) {
            accounts.addAll(accountService.getByAffiliateMasterCode(affiliateCode));

        }
        List<Invoice> invoices = invoiceService.getInvoicesBetween(startDate, endDate, accounts.stream().map(Account::getId).collect(Collectors.toSet()));

        ZoneId zoneId = ZoneId.of(tz);
        Workbook workbook = invoiceReportService.generateInvoiceReport(invoices, zoneId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
            return new ResponseEntity<>(new ByteArrayResource(baos.toByteArray()), createReportDownloadHttpHeaders(baos.toByteArray().length), HttpStatus.OK);

        } catch (IOException e) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB8103, "Failed to generate invoice report for period [" + startDate + " - " + endDate + "].");

        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                log.warn("Failed to close ByteArrayOutputStream during invoice report generation");
            }
        }
    }

    private HttpHeaders createReportDownloadHttpHeaders(
            final int dataLength
    ) {
        List<String> exposeHeaders = new ArrayList<>();
        exposeHeaders.add(HttpHeaders.CONTENT_DISPOSITION);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(exposeHeaders);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"invoice-report.xlsx\"");
        headers.setContentLength(dataLength);

        return headers;
    }

}
