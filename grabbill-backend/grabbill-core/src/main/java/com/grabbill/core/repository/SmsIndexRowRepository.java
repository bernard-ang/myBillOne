package com.grabbill.core.repository;

import com.grabbill.core.entity.SmsIndexRow;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface SmsIndexRowRepository
        extends JpaRepository<SmsIndexRow, Long>, SmsIndexRowRepositoryCustom {
}
