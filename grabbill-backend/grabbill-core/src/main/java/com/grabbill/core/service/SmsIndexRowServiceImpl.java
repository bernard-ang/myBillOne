package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.repository.SmsIndexRowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class SmsIndexRowServiceImpl
        implements BaseIndexRowService<SmsType, SmsActivity, SmsIndexRow> {

    @Autowired
    private SmsIndexRowRepository repository;

    @Autowired
    private ContactFieldService contactFieldService;


    @Override
    public Page<SmsIndexRow> searchByFilters(
            final SmsType smsType,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                smsType,
                contactFieldService.getAll(smsType.getAccount()),
                filters,
                pageable
        );
    }

    @Override
    public Page<SmsIndexRow> searchByFilters(
            final SmsType smsType,
            final SmsActivity smsActivity,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SmsIndexRow> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public SmsIndexRow save(
            final SmsIndexRow smsIndexRow
    ) {
        return repository.save(smsIndexRow);
    }

}
