package com.grabbill.core.repository;

import com.grabbill.core.entity.SmsRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface SmsRecordRepository extends JpaRepository<SmsRecord, Long> {
}
