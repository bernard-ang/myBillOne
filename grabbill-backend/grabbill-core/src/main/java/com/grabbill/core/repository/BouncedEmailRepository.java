package com.grabbill.core.repository;

import com.grabbill.core.entity.BouncedEmail;
import com.grabbill.core.model.DomainType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface BouncedEmailRepository extends JpaRepository<BouncedEmail, Long> {

    Page<BouncedEmail> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Optional<BouncedEmail> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Page<BouncedEmail> findByAccountIdAndCreatedDateIsBefore(
            Integer accountId,
            OffsetDateTime createdDate,
            Pageable pageable
    );

    Page<BouncedEmail> findByAccountIdAndCreatedDateIsAfter(
            Integer accountId,
            OffsetDateTime createdDate,
            Pageable pageable
    );

    Page<BouncedEmail> findByAccountIdAndCreatedDateIsBetween(
            Integer accountId,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Pageable pageable
    );

    Page<BouncedEmail> findByAccountIdAndEmailIsContainingIgnoreCase(
            Integer accountId,
            String email,
            Pageable pageable
    );

    Page<BouncedEmail> findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsBefore(
            Integer accountId,
            String email,
            OffsetDateTime createdDate,
            Pageable pageable
    );

    Page<BouncedEmail> findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsAfter(
            Integer accountId,
            String email,
            OffsetDateTime createdDate,
            Pageable pageable
    );

    Page<BouncedEmail> findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsBetween(
            Integer accountId,
            String email,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Pageable pageable
    );

    List<BouncedEmail> findByAccountIdAndEmailIsIgnoreCaseAndDomainTypeIs(
            Integer accountId,
            String email,
            DomainType domainType
    );

    List<BouncedEmail> findByAccountIdAndEmailIsIgnoreCase(
            Integer accountId,
            String email
    );

}
