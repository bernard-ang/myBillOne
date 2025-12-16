package com.grabbill.core.repository;

import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.StripeEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public interface StripeEventRepositoryCustom {

    Page<StripeEvent> searchStripeEvents(
            String accountName,
            StripeEventType type,
            String refId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

}
