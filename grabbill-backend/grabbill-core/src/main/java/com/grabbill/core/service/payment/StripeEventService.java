package com.grabbill.core.service.payment;

import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.StripeEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface StripeEventService {

    StripeEvent save(StripeEvent stripeEvent);

    Optional<StripeEvent> getById(Long id);

    List<StripeEvent> getByTypeAndRefId(StripeEventType stripeEventType, String refId);

    Page<StripeEvent> searchStripeEvents(
            String accountName,
            StripeEventType type,
            String refId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

}
