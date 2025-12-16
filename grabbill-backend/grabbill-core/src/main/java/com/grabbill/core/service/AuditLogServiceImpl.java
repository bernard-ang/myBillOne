package com.grabbill.core.service;

import com.grabbill.core.entity.AuditLog;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * @author michaellow
 */
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository repository;


    @Override
    public AuditLog log(
            final Integer accountId,
            Optional<Long> parentTypeId,
            final Long targetId,
            final DomainType domainType,
            final ActionType actionType,
            final String description,
            final String createdBy
    ) {
        AuditLog target = new AuditLog();
        target.setAccountId(accountId);
        parentTypeId.ifPresent(target::setParentTypeid);
        target.setTargetId(targetId);
        target.setDomainType(domainType);
        target.setActionType(actionType.toString());
        target.setDescription(description);
        target.setCreatedBy(createdBy);
        target.setCreatedDate(OffsetDateTime.now(ZoneOffset.UTC));

        if (!StringUtils.hasLength(target.getCreatedBy())) {
            target.setCreatedBy("system");
        }

        return repository.save(target);
    }

    @Override
    public Page<AuditLog> searchByFilters(
            final Integer accountId,
            final DomainType domainType,
            final String targetQuery,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        return repository.searchByFilters(accountId, domainType, targetQuery, startDate, endDate, pageable);
    }

    @Override
    public Page<AuditLog> getByUsername(
            final String username,
            final Pageable pageable
    ) {
        return repository.searchByUsername(username, pageable);
    }

}
