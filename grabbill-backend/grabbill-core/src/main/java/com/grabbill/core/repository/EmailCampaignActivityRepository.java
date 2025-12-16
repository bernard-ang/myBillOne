package com.grabbill.core.repository;

import com.grabbill.core.entity.EmailCampaignActivity;
import com.grabbill.core.entity.EmailCampaignType;
import com.grabbill.core.model.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface EmailCampaignActivityRepository extends JpaRepository<EmailCampaignActivity, Long> {

    Page<EmailCampaignActivity> findByEmailCampaignType(EmailCampaignType emailCampaignType, Pageable pageable);

    Page<EmailCampaignActivity> findByEmailCampaignTypeAndNameIsContainingIgnoreCase(EmailCampaignType emailCampaignType, String name, Pageable pageable);

    Page<EmailCampaignActivity> findByEmailCampaignTypeAndStatus(EmailCampaignType emailCampaignType, ProcessStatus status, Pageable pageable);

    Page<EmailCampaignActivity> findByEmailCampaignTypeAndNameIsContainingIgnoreCaseAndStatus(EmailCampaignType emailCampaignType, String name, ProcessStatus status, Pageable pageable);

    List<EmailCampaignActivity> findByEmailCampaignType(EmailCampaignType emailCampaignType);

    List<EmailCampaignActivity> findByEmailCampaignTypeAndStatusIn(EmailCampaignType emailCampaignType, List<ProcessStatus> statuses);

    Optional<EmailCampaignActivity> findByNameAndEmailCampaignType(String name, EmailCampaignType emailCampaignType);

    Optional<EmailCampaignActivity> findByIdAndEmailCampaignType(Long id, EmailCampaignType emailCampaignType);

    List<EmailCampaignActivity> findAllByStatusInAndPurgedTimestampIsNull(List<ProcessStatus> statuses);

    int countAllBySubmittedTimestampIsNotNull();

    List<EmailCampaignActivity> findAllByProcessedTimestampIsNotNull();

}
