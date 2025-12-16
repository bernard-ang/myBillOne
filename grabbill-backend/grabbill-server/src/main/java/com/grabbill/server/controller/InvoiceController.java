package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.model.StripeEventType;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.core.service.AccountSubscriptionService;
import com.grabbill.core.service.InvoiceService;
import com.grabbill.core.service.PromoCodeService;
import com.grabbill.core.service.payment.PaymentService;
import com.grabbill.core.service.payment.StripeEventService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AccountSubscriptionPayload;
import com.grabbill.server.controller.response.payload.InvoiceBasicPayload;
import com.grabbill.server.controller.response.payload.InvoicePayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@Slf4j
@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private StripeEventService stripeEventService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PromoCodeService promoCodeService;

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getInvoices(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Account account = userDetails.getUser().getAccount();
        Page<Invoice> page = invoiceService.getByAccount(account, pageable);

        SearchResultPayload<InvoiceBasicPayload> searchResultPayload =
                SearchResultPayload.<InvoiceBasicPayload>builder()
                        .items(page.get()
                                .map(InvoiceBasicPayload::from)
                                .collect(Collectors.toList()))
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
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer invoiceId
    ) {
        Invoice invoice = invoiceService.getByAccountAndId(userDetails.getUser().getAccount(), invoiceId).orElseThrow(
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
    @GetMapping(value = "/subscription/{accountSubscriptionId}")
    public ResponseEntity<GrabbillApiResponse> getInvoiceByAccountSubscriptionId(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer accountSubscriptionId
    ) {
        Account account = userDetails.getUser().getAccount();
        AccountSubscription accountSubscription = accountSubscriptionService.getSubscriptionByIdAndAccountId(account, accountSubscriptionId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB8101,
                        "Subscription with ID [" + accountSubscriptionId + "] is not found!"
                )
        );

        Optional<Invoice> invoiceOptional = invoiceService.getByAccountAndAccountSubscription(
                userDetails.getUser().getAccount(), accountSubscription);

        Invoice invoice;
        List<StripeEvent> stripeEvents = new ArrayList<>();
        PromoCode targetPromoCode = null;
        double totalAmountBeforeDiscount = 0;

        if (invoiceOptional.isPresent()) {
            invoice = invoiceOptional.get();
            if (invoice.getStripeInvoiceId() != null) {
                stripeEvents = stripeEventService.getByTypeAndRefId(StripeEventType.INVOICE, invoice.getStripeInvoiceId());
            }

            totalAmountBeforeDiscount = (invoice.getStoragePrice() + invoice.getEmailCampaignPrice()
                    + invoice.getTransactionalEmailPrice() + invoice.getSmsTopupPrice()) *
                    (SubscriptionMode.ANNUALLY.equals(accountSubscription.getMode()) ? 12 : 1);

            String promoCode = accountSubscription.getPromoCode();
            if (promoCode != null) {
                Optional<PromoCode> promoCodeOptional = promoCodeService.getByCode(promoCode);
                if (promoCodeOptional.isPresent()) {
                    targetPromoCode = promoCodeOptional.get();
                }
            }
        } else {
            invoice = new Invoice();
            invoice.setAccount(userDetails.getUser().getAccount());
            invoice.setTotalAmount(0d);
            invoice.setTotalTaxAmount(0d);
            invoice.setTotalAmountWithTax(0d);
        }

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        InvoicePayload.from(
                                invoice,
                                stripeEvents,
                                targetPromoCode,
                                totalAmountBeforeDiscount,
                                AccountSubscriptionPayload.from(accountSubscription))
                )
        );
    }

    @Transactional
    @PostMapping(path = "/{invoiceId}/pay")
    public ResponseEntity<GrabbillApiResponse> retryInvoicePayment(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer invoiceId
    ) {
        Invoice invoice = invoiceService.getByAccountAndId(userDetails.getUser().getAccount(), invoiceId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB8101,
                        "Invoice [" + invoiceId + "] is not found!"
                )
        );

        if (!InvoiceStatus.PAYMENT_FAILED.equals(invoice.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB8102,
                    "Invoice [" + invoiceId + "] is with status [" + invoice.getStatus().name() + "]!"
            );
        }

        com.stripe.model.Invoice stripeInvoice = paymentService.retryChargeInvoice(invoice.getStripeInvoiceId());

        ApiMessage apiMessage = null;
        if ("paid".equalsIgnoreCase(stripeInvoice.getStatus())) {
            invoiceService.markAsPaid(invoice.getId());
            log.info("Retry charge on invoice [" + invoiceId + "] is successfully, invoice is marked as paid.");

            return ResponseEntity.ok().body(
                    new GrabbillApiResponse(GrabbillServerApiVersion.V1.getVersion(),
                            new ApiMessage("Retry charge on invoice [" + invoiceId + "] is successfully, invoice is marked as paid.")
                    ));
        }

        return ResponseEntity.badRequest().body(
                new GrabbillApiResponse(GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("Retry charge on invoice [" + invoiceId + "] failed, invoice is of status [" + stripeInvoice.getStatus() + "] now.")
                ));
    }

}
