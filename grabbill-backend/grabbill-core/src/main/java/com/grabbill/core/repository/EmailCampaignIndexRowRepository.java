package com.grabbill.core.repository;

import com.grabbill.core.entity.EmailCampaignIndexRow;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface EmailCampaignIndexRowRepository
        extends JpaRepository<EmailCampaignIndexRow, Long>, EmailCampaignIndexRowRepositoryCustom {
}
