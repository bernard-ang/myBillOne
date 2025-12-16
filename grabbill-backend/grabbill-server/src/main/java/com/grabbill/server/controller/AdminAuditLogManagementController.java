package com.grabbill.server.controller;

import com.grabbill.core.entity.AdminAuditLog;
import com.grabbill.core.service.AdminAuditLogService;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AdminAuditLogPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
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
@RequestMapping("/mgmt/admin-audit-logs")
public class AdminAuditLogManagementController extends BaseManagementController {

    @Autowired
    private AdminAuditLogService adminAuditLogService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getAdminAuditLogs(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        checkStatus(userDetails);

        Page<AdminAuditLog> page = adminAuditLogService.searchByFilters(
                query,
                startDate,
                endDate,
                pageable
        );

        SearchResultPayload<AdminAuditLogPayload> searchResultPayload =
                SearchResultPayload.<AdminAuditLogPayload>builder()
                        .items(page.get()
                                .map(AdminAuditLogPayload::from)
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
