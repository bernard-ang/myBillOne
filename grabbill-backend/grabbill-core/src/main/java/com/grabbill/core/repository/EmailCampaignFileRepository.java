package com.grabbill.core.repository;

import com.grabbill.core.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface EmailCampaignFileRepository extends JpaRepository<EmailCampaignFile, Long> {

    List<EmailCampaignFile> findByEmailCampaignActivity(
            EmailCampaignActivity emailCampaignActivity
    );

    Optional<EmailCampaignFile> findByNameIsIgnoreCaseAndEmailCampaignActivity(
            String filename,
            EmailCampaignActivity emailCampaignActivity
    );

}
