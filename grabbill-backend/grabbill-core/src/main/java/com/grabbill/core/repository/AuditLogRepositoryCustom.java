package com.grabbill.core.repository;

import com.grabbill.core.entity.AuditLog;
import com.grabbill.core.model.DomainType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public interface AuditLogRepositoryCustom {

    Page<AuditLog> searchByFilters(
            Integer accountId,
            DomainType domainType,
            String query,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            Pageable pageable
    );

    Page<AuditLog> searchByUsername(
            String username,
            Pageable pageable
    );

}
