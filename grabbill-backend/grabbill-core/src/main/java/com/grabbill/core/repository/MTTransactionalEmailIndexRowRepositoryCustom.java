package com.grabbill.core.repository;

import com.grabbill.core.entity.MTTransactionalEmailActivity;
import com.grabbill.core.entity.MTTransactionalEmailIndexRow;
import com.grabbill.core.entity.MTTransactionalEmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTTransactionalEmailIndexRowRepositoryCustom {

    Page<MTTransactionalEmailIndexRow> searchByFilters(
            MTTransactionalEmailType mtTransactionalEmailType,
            Optional<MTTransactionalEmailActivity> mtTransactionalEmailActivityOptional,
            Map<String, String> filters,
            boolean hasFileOnly,
            Pageable pageable
    );

}
