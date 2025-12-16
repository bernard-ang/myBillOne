package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.WhatsAppEvent;
import com.grabbill.core.repository.WhatsAppEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public class WhatsAppEventServiceImpl implements WhatsAppEventService {

    @Autowired
    private WhatsAppEventRepository repository;


    @Override
    public WhatsAppEvent save(final WhatsAppEvent event) {
        return repository.save(event);
    }

    @Override
    public Optional<WhatsAppEvent> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public List<WhatsAppEvent> findAllByProcessedIsFalse() {
        return repository.findAllByProcessedIsFalseAndProcessedCountLessThan(5);
    }

    @Override
    public Page<WhatsAppEvent> searchUserInitiatedEvents(
            final Integer accountId,
            final String messageType,
            final String refId,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        return repository.searchWhatsAppEvents(accountId, messageType, refId, startDate, endDate, pageable);
    }

}
