package com.grabbill.core.service;

import com.grabbill.core.entity.EmailCampaignActivity;
import com.grabbill.core.entity.EmailCampaignIndexRow;
import com.grabbill.core.entity.EmailCampaignType;
import com.grabbill.core.repository.EmailCampaignIndexRowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class EmailCampaignIndexRowServiceImpl
        implements BaseIndexRowService<EmailCampaignType, EmailCampaignActivity, EmailCampaignIndexRow> {

    @Autowired
    private EmailCampaignIndexRowRepository repository;

    @Autowired
    private ContactFieldService contactFieldService;


    @Override
    public Page<EmailCampaignIndexRow> searchByFilters(
            final EmailCampaignType emailCampaignType,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        return repository.searchByFilters(
                emailCampaignType,
                contactFieldService.getAll(emailCampaignType.getAccount()),
                filters,
                pageable
        );
    }

    @Override
    public Page<EmailCampaignIndexRow> searchByFilters(
            final EmailCampaignType transactionalEmailType,
            final EmailCampaignActivity emailCampaignActivity,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<EmailCampaignIndexRow> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public EmailCampaignIndexRow save(
            final EmailCampaignIndexRow emailCampaignIndexRow
    ) {
        return repository.save(emailCampaignIndexRow);
    }

}
