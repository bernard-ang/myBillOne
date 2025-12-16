package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.WhatsAppEvent;
import com.grabbill.core.service.whatsapp.WhatsAppEventService;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.controller.response.payload.whatsapp.WhatsAppEventBasicPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

/**
 * @author seez
 */
@RestController
@RequestMapping("/whatsapp/events")
public class WhatsAppEventController {

    @Autowired
    private WhatsAppEventService whatsAppEventService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getEvents(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String messageType,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Account account = userDetails.getUser().getAccount();

        Page<WhatsAppEvent> page = whatsAppEventService.searchUserInitiatedEvents(
                account.getId(),
                messageType,
                mobileNo,
                startDate,
                endDate,
                pageable
        );

        SearchResultPayload<WhatsAppEventBasicPayload> searchResultPayload =
                SearchResultPayload.<WhatsAppEventBasicPayload>builder()
                        .items(page.get()
                                .map(WhatsAppEventBasicPayload::from)
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

}
