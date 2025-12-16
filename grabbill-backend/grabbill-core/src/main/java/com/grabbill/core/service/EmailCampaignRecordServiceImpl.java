package com.grabbill.core.service;

import com.grabbill.core.entity.EmailCampaignRecord;
import com.grabbill.core.repository.EmailCampaignRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author michaellow
 */
public class EmailCampaignRecordServiceImpl implements BaseRecordService<EmailCampaignRecord> {

    @Autowired
    private EmailCampaignRecordRepository repository;


    @Override
    public EmailCampaignRecord save(final EmailCampaignRecord emailCampaignRecord) {
        return repository.save(emailCampaignRecord);
    }

}
