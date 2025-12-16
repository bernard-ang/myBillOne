package com.grabbill.core.repository;

import com.grabbill.core.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author michaellow
 */
public interface EmailCampaignIndexRowRepositoryCustom {

    Page<EmailCampaignIndexRow> searchByFilters(
            EmailCampaignType emailCampaignType,
            List<ContactField> contactFields,
            Map<String, String> filters,
            Pageable pageable
    );

}
