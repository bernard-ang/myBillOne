package com.grabbill.core.service;

import com.grabbill.core.entity.MTTransactionalEmailActivity;
import com.grabbill.core.entity.MTTransactionalEmailIndexRow;
import com.grabbill.core.entity.MTTransactionalEmailType;
import com.grabbill.core.repository.MTTransactionalEmailIndexRowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class MTTransactionalEmailIndexRowServiceImpl
        implements BaseIndexRowService<MTTransactionalEmailType, MTTransactionalEmailActivity, MTTransactionalEmailIndexRow> {

    @Autowired
    private MTTransactionalEmailIndexRowRepository repository;


    @Override
    public Page<MTTransactionalEmailIndexRow> searchByFilters(
            final MTTransactionalEmailType mtTransactionalEmailType,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                mtTransactionalEmailType,
                Optional.empty(),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Page<MTTransactionalEmailIndexRow> searchByFilters(
            final MTTransactionalEmailType mtTransactionalEmailType,
            final MTTransactionalEmailActivity mtTransactionalEmailActivity,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                mtTransactionalEmailType,
                Optional.of(mtTransactionalEmailActivity),
                filters,
                hasFileOnly,
                pageable
        );
    }

    @Override
    public Optional<MTTransactionalEmailIndexRow> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public MTTransactionalEmailIndexRow save(
            final MTTransactionalEmailIndexRow mtTransactionalEmailIndexRow
    ) {
        return repository.save(mtTransactionalEmailIndexRow);
    }

}
