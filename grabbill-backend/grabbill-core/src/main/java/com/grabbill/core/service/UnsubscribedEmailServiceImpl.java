package com.grabbill.core.service;

import com.grabbill.core.entity.UnsubscribedEmail;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.repository.UnsubscribedEmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class UnsubscribedEmailServiceImpl implements UnsubscribedEmailService {

    @Autowired
    private UnsubscribedEmailRepository repository;


    @Override
    public Page<UnsubscribedEmail> searchByFilters(
            final Integer accountId,
            final String email,
            final OffsetDateTime startDateTime,
            final OffsetDateTime endDateTime,
            Pageable pageable
    ) {
        if (StringUtils.hasLength(email)) {
            if (startDateTime != null && endDateTime != null) {
                return repository.findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsBetween(
                        accountId,
                        email,
                        startDateTime,
                        endDateTime,
                        pageable
                );

            } else if (startDateTime != null) {
                return repository.findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsAfter(
                        accountId,
                        email,
                        startDateTime,
                        pageable
                );

            } else if (endDateTime != null) {
                return repository.findByAccountIdAndEmailIsContainingIgnoreCaseAndCreatedDateIsBefore(
                        accountId,
                        email,
                        endDateTime,
                        pageable
                );
            }

            return repository.findByAccountIdAndEmailIsContainingIgnoreCase(
                    accountId,
                    email,
                    pageable
            );

        } else {
            if (startDateTime != null && endDateTime != null) {
                return repository.findByAccountIdAndCreatedDateIsBetween(
                        accountId,
                        startDateTime,
                        endDateTime,
                        pageable
                );

            } else if (startDateTime != null) {
                return repository.findByAccountIdAndCreatedDateIsAfter(
                        accountId,
                        startDateTime,
                        pageable
                );

            } else if (endDateTime != null) {
                return repository.findByAccountIdAndCreatedDateIsBefore(
                        accountId,
                        endDateTime,
                        pageable
                );
            }
        }

        return repository.findByAccountId(accountId, pageable);
    }

    @Override
    public List<UnsubscribedEmail> get(
            final Integer accountId,
            final String email,
            final DomainType domainType,
            final Long typeId
    ) {
        return repository.findByAccountIdAndEmailIsIgnoreCaseAndDomainTypeIsAndTypeIdIs(
                accountId,
                email,
                domainType,
                typeId
        );
    }

    @Override
    public boolean isUnsubscribed(
            final Integer accountId,
            final String email,
            final DomainType domainType,
            final Long typeId
    ) {
        return !repository.findByAccountIdAndEmailIsIgnoreCaseAndDomainTypeIsAndTypeIdIs(
                accountId,
                email,
                domainType,
                typeId
        ).isEmpty();
    }

    @Override
    public int totalUnsubscribedEmailByType(Integer accountId, DomainType domainType, Long typeId) {
        return repository.findByAccountIdAndDomainTypeIsAndTypeIdIs(
                accountId,
                domainType,
                typeId
        ).size();
    }

    @Override
    public int totalUnsubscribedEmailByActivity(Integer accountId, DomainType domainType, Long typeId, Long activityId) {
        return repository.findByAccountIdAndDomainTypeIsAndTypeIdIsAndActivityIdIs(
                accountId,
                domainType,
                typeId,
                activityId
        ).size();
    }

    @Override
    public UnsubscribedEmail save(final UnsubscribedEmail unsubscribedEmail) {
        return repository.save(unsubscribedEmail);
    }

    @Override
    public Optional<UnsubscribedEmail> getById(User user, Long id) {
        return repository.findByIdAndAccountId(id, user.getAccount().getId());
    }

    @Override
    public void delete(UnsubscribedEmail unsubscribedEmail) {
        repository.delete(unsubscribedEmail);
    }
}
