package com.grabbill.core.service;

import com.grabbill.core.entity.AdminAuditLog;
import com.grabbill.core.repository.AdminAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    @Autowired
    private AdminAuditLogRepository repository;


    @Override
    public Page<AdminAuditLog> searchByFilters(
            final String query,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        return repository.searchByFilters(query, startDate, endDate, pageable);
    }

    @Override
    public AdminAuditLog save(final AdminAuditLog adminAuditLog) {
        return repository.save(adminAuditLog);
    }

}
