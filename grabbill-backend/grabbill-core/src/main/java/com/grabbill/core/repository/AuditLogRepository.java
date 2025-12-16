package com.grabbill.core.repository;

import com.grabbill.core.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, AuditLogRepositoryCustom {
}
