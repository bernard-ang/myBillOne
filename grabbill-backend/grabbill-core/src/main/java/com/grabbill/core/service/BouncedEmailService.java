package com.grabbill.core.service;

import com.grabbill.core.entity.BouncedEmail;
import com.grabbill.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface BouncedEmailService {

    Page<BouncedEmail> searchByFilters(
            Integer accountId,
            String email,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime,
            Pageable pageable
    );

    boolean hasBouncedEmail(
            Integer accountId,
            String email
    );

    Optional<BouncedEmail> getById(User user, Long id);

    void delete(BouncedEmail bouncedEmail);

    BouncedEmail save(BouncedEmail bouncedEmail);

}
