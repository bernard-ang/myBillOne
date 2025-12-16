package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppIndexRow;
import com.grabbill.core.entity.WhatsAppType;
import com.grabbill.core.repository.WhatsAppIndexRowRepository;
import com.grabbill.core.service.BaseIndexRowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author seez
 */
public class WhatsAppIndexRowServiceImpl
        implements BaseIndexRowService<WhatsAppType, WhatsAppActivity, WhatsAppIndexRow> {

    @Autowired
    private WhatsAppIndexRowRepository repository;


    @Override
    public Page<WhatsAppIndexRow> searchByFilters(
            final WhatsAppType whatsAppType,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                whatsAppType,
                Optional.empty(),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Page<WhatsAppIndexRow> searchByFilters(
            final WhatsAppType whatsAppType,
            final WhatsAppActivity whatsAppActivity,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                whatsAppType,
                Optional.of(whatsAppActivity),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Optional<WhatsAppIndexRow> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public WhatsAppIndexRow save(
            final WhatsAppIndexRow whatsAppIndexRow
    ) {
        return repository.save(whatsAppIndexRow);
    }

}
