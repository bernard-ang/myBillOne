package com.grabbill.server.controller;

import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.StripeEventType;
import com.grabbill.core.service.payment.StripeEventService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.controller.response.payload.StripeEventBasicPayload;
import com.grabbill.server.controller.response.payload.StripeEventPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/mgmt/events")
public class StripeEventManagementController extends BaseManagementController {

    @Autowired
    private StripeEventService stripeEventService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getEvents(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) StripeEventType type,
            @RequestParam(required = false) String refId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        checkStatus(userDetails);

        Page<StripeEvent> page = stripeEventService.searchStripeEvents(
                accountName,
                type,
                refId,
                startDate,
                endDate,
                pageable
        );

        SearchResultPayload<StripeEventBasicPayload> searchResultPayload =
                SearchResultPayload.<StripeEventBasicPayload>builder()
                        .items(page.get()
                                .map(StripeEventBasicPayload::from)
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
    @GetMapping(path = "/{eventId}")
    public ResponseEntity<GrabbillApiResponse> getPaymentTransactionById(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Long eventId
    ) {
        checkStatus(userDetails);

        StripeEvent target = stripeEventService.getById(eventId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB8001,
                        "Payment event with ID [" + eventId + "] is not found!"
                )
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        StripeEventPayload.from(target)
                )
        );
    }

}
