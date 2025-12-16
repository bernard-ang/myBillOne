package com.grabbill.core.repository;

import com.grabbill.core.entity.DigitalFilingIndexRow;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface DigitalFilingIndexRowRepository
        extends JpaRepository<DigitalFilingIndexRow, Long>, DigitalFilingIndexRowRepositoryCustom {
}
