package com.grabbill.core.repository;

import com.grabbill.core.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface TransactionalEmailIndexRowRepositoryCustom {

    Page<TransactionalEmailIndexRow> searchByFilters(
            TransactionalEmailType transactionalEmailType,
            Optional<TransactionalEmailActivity> transactionalEmailActivityOptional,
            Map<String, String> filters,
            boolean hasFileOnly,
            Pageable pageable
    );

}
