package com.grabbill.core.repository;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Job;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * @author seez
 */
public interface AccountRepositoryCustom {

    Page<Account> searchAccount(
            String companyName,
            String planName,
            final OffsetDateTime startCreatedDate,
            final OffsetDateTime endCreatedDate,
            Pageable pageable
    );

}
