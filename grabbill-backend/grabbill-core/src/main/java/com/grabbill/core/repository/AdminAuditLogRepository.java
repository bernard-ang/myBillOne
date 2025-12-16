package com.grabbill.core.repository;

import com.grabbill.core.entity.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long>, AdminAuditLogRepositoryCustom {
}
