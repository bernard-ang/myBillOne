package com.grabbill.core.repository;

import com.grabbill.core.entity.UnsubscribedEmail;
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
public interface UnsubscribedEmailRepository extends JpaRepository<UnsubscribedEmail, Long> {

    Page<UnsubscribedEmail> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Optional<UnsubscribedEmail> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Page<UnsubscribedEmail> findByAccountIdAndCreatedDateIsBefore(
            Integer accountId,
            OffsetDateTime createdDate,
            Pageable pageable
    );

    Page<UnsubscribedEmail> findByAccountIdAndCreatedDateIsAfter(
            Integer accountId,
            OffsetDateTime createdDate,
            Pageable pageable
    );

    Page<UnsubscribedEmail> findByAccountIdAndCreatedDateIsBetween(
            Integer accountId,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Pageable pageable
    );

    Page<UnsubscribedEmail> findByAccountIdAndEmailIsContainingIgnoreCase(
            Integer accountId,
            String email,
            Pageable pageable
    );

    Page<UnsubscribedEmail> findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsBefore(
            Integer accountId,
            String email,
            OffsetDateTime createdDate,
            Pageable pageable
    );

    Page<UnsubscribedEmail> findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsAfter(
            Integer accountId,
            String email,
            OffsetDateTime createdDate,
            Pageable pageable
    );

    Page<UnsubscribedEmail> findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsBetween(
            Integer accountId,
            String email,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Pageable pageable
    );

    List<UnsubscribedEmail> findByAccountIdAndEmailIsIgnoreCaseAndDomainTypeIsAndTypeIdIs(
            Integer accountId,
            String email,
            DomainType domainType,
            Long typeId
    );

    List<UnsubscribedEmail> findByAccountIdAndDomainTypeIsAndTypeIdIs(
            Integer accountId,
            DomainType domainType,
            Long typeId
    );

    List<UnsubscribedEmail> findByAccountIdAndDomainTypeIsAndTypeIdIsAndActivityIdIs(
            Integer accountId,
            DomainType domainType,
            Long typeId,
            Long activityId
    );

}
