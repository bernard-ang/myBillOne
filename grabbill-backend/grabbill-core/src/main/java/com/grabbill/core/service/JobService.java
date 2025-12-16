package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Job;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface JobService {

    List<Job> getAllImmediateJobs();

    List<Job> getAllScheduleReadyJobs();

    Optional<Job> getById(long id);

    Optional<Job> getByIdAndAccount(long id, Account account);

    Job save(Job job);

    Job saveAndFlush(Job job);

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
