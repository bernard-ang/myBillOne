package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * @author seez
 */
public interface WhatsAppEventRepositoryCustom {

    Page<WhatsAppEvent> searchWhatsAppEvents(
            Integer accountId,
            String messageType,
            String mobileNo,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

}
