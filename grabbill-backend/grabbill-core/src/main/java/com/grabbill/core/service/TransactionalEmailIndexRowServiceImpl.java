package com.grabbill.core.service;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailIndexRow;
import com.grabbill.core.entity.TransactionalEmailType;
import com.grabbill.core.repository.TransactionalEmailIndexRowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class TransactionalEmailIndexRowServiceImpl
        implements BaseIndexRowService<TransactionalEmailType, TransactionalEmailActivity, TransactionalEmailIndexRow> {

    @Autowired
    private TransactionalEmailIndexRowRepository repository;


    @Override
    public Page<TransactionalEmailIndexRow> searchByFilters(
            final TransactionalEmailType transactionalEmailType,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                transactionalEmailType,
                Optional.empty(),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Page<TransactionalEmailIndexRow> searchByFilters(
            final TransactionalEmailType transactionalEmailType,
            final TransactionalEmailActivity transactionalEmailActivity,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                transactionalEmailType,
                Optional.of(transactionalEmailActivity),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Optional<TransactionalEmailIndexRow> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public TransactionalEmailIndexRow save(
            final TransactionalEmailIndexRow transactionalEmailIndexRow
    ) {
        return repository.save(transactionalEmailIndexRow);
    }

}
