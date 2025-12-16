package com.grabbill.core.service;

import com.grabbill.core.entity.UnsubscribedEmail;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.DomainType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface UnsubscribedEmailService {

    Page<UnsubscribedEmail> searchByFilters(
            Integer accountId,
            String email,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Pageable pageable
    );

    List<UnsubscribedEmail> get(
            Integer accountId,
            String email,
            DomainType domainType,
            Long typeId
    );

    boolean isUnsubscribed(
            Integer accountId,
            String email,
            DomainType domainType,
            Long typeId
    );

    int totalUnsubscribedEmailByType(
            Integer accountId,
            DomainType domainType,
            Long typeId
    );

    int totalUnsubscribedEmailByActivity(
            Integer accountId,
            DomainType domainType,
            Long typeId,
            Long activityId
    );

    UnsubscribedEmail save(
            UnsubscribedEmail unsubscribedEmail
    );

    Optional<UnsubscribedEmail> getById(User user, Long id);

    void delete(UnsubscribedEmail bouncedEmail);
}
