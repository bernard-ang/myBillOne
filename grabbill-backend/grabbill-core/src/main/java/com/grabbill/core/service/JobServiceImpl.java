package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Job;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobExecutionMode;
import com.grabbill.core.model.job.JobStatus;
import com.grabbill.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository repository;


    @Override
    public List<Job> getAllImmediateJobs() {
        return repository.findByExecutionModeAndStatusIn(
                JobExecutionMode.IMMEDIATE,
                Collections.singleton(JobStatus.NEW)
        );
    }

    @Override
    public List<Job> getAllScheduleReadyJobs() {
        return repository.findByExecutionModeAndStatusInAndScheduledExecutionTimestampLessThanEqual(
                JobExecutionMode.SCHEDULED,
                Collections.singleton(JobStatus.NEW),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    @Override
    public Optional<Job> getById(final long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Job> getByIdAndAccount(final long id, final Account account) {
        return repository.findByIdAndAccount(id, account);
    }

    @Override
    public Job save(final Job job) {
        return repository.save(job);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Job saveAndFlush(final Job job) {
        return repository.saveAndFlush(job);
    }

    @Override
    public Page<Job> searchJobs(
            final String accountName,
            final DomainType domainType,
            final String activity,
            final JobStatus status,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        return repository.searchJobs(accountName, domainType, activity, status, startDate, endDate, pageable);
    }

}
