package com.grabbill.server.controller;

import com.grabbill.core.conf.DeploymentProperties;
import com.grabbill.core.entity.*;
import com.grabbill.core.model.*;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.JobService;
import com.grabbill.core.service.UserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.JobBasicPayload;
import com.grabbill.server.controller.response.payload.JobPayload;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private DeploymentProperties deploymentProperties;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @Autowired
    private JobControllerService jobControllerService;


    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getJobs(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) DomainType domainType,
            @RequestParam(required = false) String activity,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @PageableDefault(sort = {"createdTimestamp"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        verifyIfOnPremDeployment();
        User user = getUser(userDetails.getUsername());

        Page<Job> page = jobService.searchJobs(
                user.getAccount().getCompanyName(),
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
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long jobId
    ) {
        verifyIfOnPremDeployment();
        User user = getUser(userDetails.getUsername());

        Job target = jobService.getByIdAndAccount(jobId, user.getAccount()).orElseThrow(
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
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Long jobId
    ) {
        verifyIfOnPremDeployment();
        User user = getUser(userDetails.getUsername());

        Job targetJob = jobService.getByIdAndAccount(jobId, user.getAccount()).orElseThrow(
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

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                jobId,
                DomainType.JOB,
                ActionType.JOB_RETRY,
                "Retry triggered for job with id [" + jobId + "].",
                userDetails.getUsername()
        );

        JobPayload jobPayload = JobPayload.from(updatedJob);
        jobControllerService.copyToPayload(jobPayload, activity);

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        jobPayload
                )
        );
    }

    protected void verifyIfOnPremDeployment() {
        if (!deploymentProperties.isOnPremise()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB0016,
                    "Current deployment mode is [" + deploymentProperties.getMode() + "]!"
            );
        }
    }

    private User getUser(final String email) {
        return userService.getByEmail(email).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1002,
                        "User [" + email + "] is not found!"
                )
        );
    }

}
