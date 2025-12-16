package com.grabbill.core.repository;

import com.grabbill.core.entity.EmailCampaignRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface EmailCampaignRecordRepository extends JpaRepository<EmailCampaignRecord, Long> {
}
