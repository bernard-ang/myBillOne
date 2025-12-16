package com.grabbill.server.controller;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.AdminActionType;
import com.grabbill.core.model.AdminDomainType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.service.AdminAuditLogService;
import com.grabbill.core.service.JobService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.JobBasicPayload;
import com.grabbill.server.controller.response.payload.JobPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.JobControllerService;
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
@RequestMapping("/mgmt/jobs")
public class JobManagementController extends BaseManagementController {

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobControllerService jobControllerService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getJobs(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) DomainType domainType,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"createdTimestamp"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        checkStatus(userDetails);

        Page<Job> page = jobService.searchJobs(
                accountName,
                domainType,
                activity,
                status,
                startDate,
                endDate,
                pageable
        );

        SearchResultPayload<JobBasicPayload> searchResultPayload =
                SearchResultPayload.<JobBasicPayload>builder()
                        .items(page.get()
                                .map(JobBasicPayload::from)
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
    @GetMapping(path = "/{jobId}")
    public ResponseEntity<GrabbillApiResponse> getJobById(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Long jobId
    ) {
        checkStatus(userDetails);

        Job target = jobService.getById(jobId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1022,
                        "Job with ID [" + jobId + "] is not found!"
                )
        );
        JobPayload jobPayload = JobPayload.from(target);
        BaseActivity activity = jobControllerService.getActivity(target);
        jobControllerService.copyToPayload(jobPayload, activity);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        jobPayload
                )
        );
    }

    @Transactional
    @PostMapping(path = "/{jobId}/retry")
    public ResponseEntity<GrabbillApiResponse> retryJobById(
            @AuthenticationPrincipal GrabbillAdminUserDetails userDetails,
            @PathVariable Long jobId
    ) {
        checkStatus(userDetails);

        Job targetJob = jobService.getById(jobId).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1022,
                        "Job with ID [" + jobId + "] is not found!"
                )
        );

        BaseActivity activity = jobControllerService.getActivity(targetJob);
        if (!ProcessStatus.SUBMITTED.equals(activity.getStatus())
                && !ProcessStatus.ERROR.equals(activity.getStatus())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1025,
                    "Activity of domain type [" + targetJob.getDomainType() + "] with ID ["
                            + targetJob.getActivityId() + "] is not in correct state for retry!"
            );
        }

        // mark as new and retry, so job scheduler will pick it up and publish to job-queue again
        targetJob.setRetry(true);
        targetJob.setStatus(JobStatus.NEW);
        Job updatedJob = jobService.saveAndFlush(targetJob);

        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setAdminDomainType(AdminDomainType.JOB);
        adminAuditLog.setTargetId(jobId.intValue());
        adminAuditLog.setActionType(AdminActionType.JOB_RETRY.name());
        adminAuditLog.setDescription("Retry triggered for job with id [" + jobId + "].");
        adminAuditLogService.save(adminAuditLog);

        JobPayload jobPayload = JobPayload.from(updatedJob);
        jobControllerService.copyToPayload(jobPayload, activity);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        jobPayload
                )
        );
    }

}
