package com.grabbill.core.repository;

import com.grabbill.core.entity.TransactionalEmailIndexRow;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface TransactionalEmailIndexRowRepository
        extends JpaRepository<TransactionalEmailIndexRow, Long>, TransactionalEmailIndexRowRepositoryCustom {
}
