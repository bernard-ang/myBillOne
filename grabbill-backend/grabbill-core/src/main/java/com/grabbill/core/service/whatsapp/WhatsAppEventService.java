package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.WhatsAppEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public interface WhatsAppEventService {

    WhatsAppEvent save(WhatsAppEvent event);

    Optional<WhatsAppEvent> getById(Long id);

    List<WhatsAppEvent> findAllByProcessedIsFalse();

    Page<WhatsAppEvent> searchUserInitiatedEvents(
            final Integer accountId,
            final String messageType,
            final String refId,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    );
}
