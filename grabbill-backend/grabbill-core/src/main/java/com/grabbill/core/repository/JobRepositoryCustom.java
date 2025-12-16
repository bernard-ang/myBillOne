package com.grabbill.core.repository;

import com.grabbill.core.entity.Job;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public interface JobRepositoryCustom {

    Page<Job> searchJobs(
            String accountName,
            DomainType domainType,
            String activity,
            JobStatus status,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            Pageable pageable
    );

}
