package com.grabbill.core.repository;

import com.grabbill.core.entity.DigitalFilingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface DigitalFilingRecordRepository extends JpaRepository<DigitalFilingRecord, Long> {
}
