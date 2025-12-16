package com.grabbill.core.service;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingIndexRow;
import com.grabbill.core.entity.DigitalFilingType;
import com.grabbill.core.repository.DigitalFilingIndexRowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class DigitalFilingIndexRowServiceImpl
        implements BaseIndexRowService<DigitalFilingType, DigitalFilingActivity, DigitalFilingIndexRow> {

    @Autowired
    private DigitalFilingIndexRowRepository repository;


    @Override
    public Page<DigitalFilingIndexRow> searchByFilters(
            final DigitalFilingType digitalFilingType,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                digitalFilingType,
                Optional.empty(),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Page<DigitalFilingIndexRow> searchByFilters(
            final DigitalFilingType digitalFilingType,
            final DigitalFilingActivity digitalFilingActivity,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                digitalFilingType,
                Optional.of(digitalFilingActivity),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Optional<DigitalFilingIndexRow> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public DigitalFilingIndexRow save(final DigitalFilingIndexRow digitalFilingIndexRow) {
        return repository.save(digitalFilingIndexRow);
    }

}
