package com.grabbill.core.repository;

import com.grabbill.core.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public interface AdminAuditLogRepositoryCustom {

    Page<AdminAuditLog> searchByFilters(
            String query,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            Pageable pageable
    );

}
