package com.grabbill.core.service.payment;

import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.StripeEventType;
import com.grabbill.core.repository.StripeEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class StripeEventServiceImpl implements StripeEventService {

    @Autowired
    private StripeEventRepository repository;


    @Override
    public StripeEvent save(final StripeEvent stripeEvent) {
        return repository.save(stripeEvent);
    }

    @Override
    public Optional<StripeEvent> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public List<StripeEvent> getByTypeAndRefId(
            final StripeEventType stripeEventType,
            final String refId
    ) {
        return repository.findByTypeAndRefId(stripeEventType, refId);
    }

    @Override
    public Page<StripeEvent> searchStripeEvents(
            final String accountName,
            final StripeEventType type,
            final String refId,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        return repository.searchStripeEvents(accountName, type, refId, startDate, endDate, pageable);
    }

}
