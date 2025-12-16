package com.grabbill.core.service;

import com.grabbill.core.entity.BouncedEmail;
import com.grabbill.core.entity.User;
import com.grabbill.core.repository.BouncedEmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * @author michaellow
 */
public class BouncedEmailServiceImpl implements BouncedEmailService {

    @Autowired
    private BouncedEmailRepository repository;


    @Override
    public Page<BouncedEmail> searchByFilters(
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
    public boolean hasBouncedEmail(
            final Integer accountId,
            final String email
    ) {
        return !repository.findByAccountIdAndEmailIsIgnoreCase(
                accountId,
                email
        ).isEmpty();
    }

    @Override
    public Optional<BouncedEmail> getById(final User user, final Long id) {
        return repository.findByIdAndAccountId(id, user.getAccount().getId());
    }

    @Override
    public void delete(final BouncedEmail bouncedEmail) {
        repository.delete(bouncedEmail);
    }

    @Override
    public BouncedEmail save(final BouncedEmail bouncedEmail) {
        return repository.save(bouncedEmail);
    }

}
