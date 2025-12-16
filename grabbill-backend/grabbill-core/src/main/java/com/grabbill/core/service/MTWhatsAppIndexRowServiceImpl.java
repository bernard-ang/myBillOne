package com.grabbill.core.service;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppIndexRow;
import com.grabbill.core.entity.MTWhatsAppType;
import com.grabbill.core.repository.MTWhatsAppIndexRowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class MTWhatsAppIndexRowServiceImpl
        implements BaseIndexRowService<MTWhatsAppType, MTWhatsAppActivity, MTWhatsAppIndexRow> {

    @Autowired
    private MTWhatsAppIndexRowRepository repository;


    @Override
    public Page<MTWhatsAppIndexRow> searchByFilters(
            final MTWhatsAppType mtWhatsAppType,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                mtWhatsAppType,
                Optional.empty(),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Page<MTWhatsAppIndexRow> searchByFilters(
            final MTWhatsAppType mtWhatsAppType,
            final MTWhatsAppActivity mtWhatsAppActivity,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                mtWhatsAppType,
                Optional.of(mtWhatsAppActivity),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Optional<MTWhatsAppIndexRow> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public MTWhatsAppIndexRow save(
            final MTWhatsAppIndexRow mtWhatsAppIndexRow
    ) {
        return repository.save(mtWhatsAppIndexRow);
    }

}
