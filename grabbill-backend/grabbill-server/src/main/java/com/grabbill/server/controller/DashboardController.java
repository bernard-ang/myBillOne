package com.grabbill.server.controller;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.AuditLog;
import com.grabbill.core.service.AccountSubscriptionService;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.PlanUsageService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.AuditLogPayload;
import com.grabbill.server.controller.response.payload.DashboardStatisticsPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PlanUsageService planUsageService;


    @Transactional
    @GetMapping(value = "/current-statistics")
    public ResponseEntity<GrabbillApiResponse> loadCurrentStatistics(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        Account account = userDetails.getUser().getAccount();
        AccountSubscription currentSubscription = accountSubscriptionService.getActiveSubscriptionByAccountId(account.getId())
                .orElseThrow(
                        () -> new GrabbillServerException(
                                GrabbillServerErrorCode.GRB1020,
                                "No active subscription plan found!"
                        )
                );

        DashboardStatisticsPayload payload = DashboardStatisticsPayload.from(
                currentSubscription.getCycleStartDate(),
                currentSubscription.getCycleEndDate(),
                planUsageService.calcStorageUsage(account, currentSubscription),
                planUsageService.calcTransactionalEmailUsage(account, currentSubscription),
                planUsageService.calcEmailCampaignUsage(account, currentSubscription),
                planUsageService.getSmsRemainingCredits(account),
                planUsageService.getSmsTotalCreditsUsed(
                        account,
                        currentSubscription.getCycleStartDate(),
                        currentSubscription.getCycleEndDate()
                )
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        payload
                )
        );
    }

    @Transactional
    @GetMapping(value = "/recent-activities")
    public ResponseEntity<GrabbillApiResponse> searchRecentActivities(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AuditLog> page = auditLogService.getByUsername(userDetails.getUsername(), pageable);

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
