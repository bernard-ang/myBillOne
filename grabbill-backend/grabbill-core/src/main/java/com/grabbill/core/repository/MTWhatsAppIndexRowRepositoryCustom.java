package com.grabbill.core.repository;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppIndexRow;
import com.grabbill.core.entity.MTWhatsAppType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTWhatsAppIndexRowRepositoryCustom {

    Page<MTWhatsAppIndexRow> searchByFilters(
            MTWhatsAppType mtWhatsappType,
            Optional<MTWhatsAppActivity> mtWhatsappActivityOptional,
            Map<String, String> filters,
            boolean hasFileOnly,
            Pageable pageable
    );

}
