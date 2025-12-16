package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppIndexRow;
import com.grabbill.core.entity.WhatsAppType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface WhatsAppIndexRowRepositoryCustom {

    Page<WhatsAppIndexRow> searchByFilters(
            WhatsAppType whatsappType,
            Optional<WhatsAppActivity> whatsappActivityOptional,
            Map<String, String> filters,
            boolean hasFileOnly,
            Pageable pageable
    );

}
