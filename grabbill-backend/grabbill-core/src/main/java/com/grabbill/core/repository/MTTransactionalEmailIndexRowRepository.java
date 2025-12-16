package com.grabbill.core.repository;

import com.grabbill.core.entity.MTTransactionalEmailIndexRow;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface MTTransactionalEmailIndexRowRepository
        extends JpaRepository<MTTransactionalEmailIndexRow, Long>, MTTransactionalEmailIndexRowRepositoryCustom {
}
