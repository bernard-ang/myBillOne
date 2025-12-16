package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AuditLog;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AuditLogPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
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
 * @author michaellow
 */
@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> filterAuditLogs(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String domainType,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Account account = userDetails.getUser().getAccount();
        Page<AuditLog> page = auditLogService.searchByFilters(
                account.getId(),
                DomainType.from(domainType),
                query,
                startDate,
                endDate,
                pageable
        );

        SearchResultPayload<AuditLogPayload> searchResultPayload =
                SearchResultPayload.<AuditLogPayload>builder()
                        .items(page.get()
                                .map(AuditLogPayload::from)
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
