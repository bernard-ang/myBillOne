package com.grabbill.core.service;

import com.grabbill.core.entity.AuditLog;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface AuditLogService {

    AuditLog log(
            Integer accountId,
            Optional<Long> parentTypeId,
            Long targetId,
            DomainType domainType,
            ActionType actionType,
            String description,
            String createdBy
    );

    Page<AuditLog> searchByFilters(
            Integer accountId,
            DomainType domainType,
            String targetQuery,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            Pageable pageable
    );

    Page<AuditLog> getByUsername(
            String username,
            Pageable pageable
    );

}
