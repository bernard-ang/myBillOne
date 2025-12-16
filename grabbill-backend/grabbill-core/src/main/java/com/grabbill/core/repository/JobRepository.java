package com.grabbill.core.repository;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Job;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface JobRepository extends JpaRepository<Job, Long>, JobRepositoryCustom {

    List<Job> findByDomainTypeAndActivityId(
            DomainType domainType,
            long activityId
    );

    List<Job> findByExecutionModeAndStatusIn(
            JobExecutionMode executionMode,
            Set<JobStatus> statuses
    );

    List<Job> findByExecutionModeAndStatusInAndScheduledExecutionTimestampLessThanEqual(
            JobExecutionMode executionMode,
            Set<JobStatus> statuses,
            OffsetDateTime scheduledExecutionTimestamp
    );

    Optional<Job> findByIdAndAccount(Long id, Account account);

}
