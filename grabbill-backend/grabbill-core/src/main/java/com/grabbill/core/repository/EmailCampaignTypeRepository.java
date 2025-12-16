package com.grabbill.core.repository;

import com.grabbill.core.entity.EmailCampaignType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface EmailCampaignTypeRepository extends JpaRepository<EmailCampaignType, Long> {

    Optional<EmailCampaignType> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Optional<EmailCampaignType> findByNameAndAccountId(
            String name,
            Integer accountId
    );

    Page<EmailCampaignType> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Page<EmailCampaignType> findByAccountIdAndCodeIn(
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    Page<EmailCampaignType> findByNameIsContainingIgnoreCaseAndAccountId(
            String name,
            Integer accountId,
            Pageable pageable
    );

    Page<EmailCampaignType> findByNameIsContainingIgnoreCaseAndAccountIdAndCodeIn(
            String name,
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    List<EmailCampaignType> findByNameStartsWithOrderByNameAsc(
            String name
    );

}
