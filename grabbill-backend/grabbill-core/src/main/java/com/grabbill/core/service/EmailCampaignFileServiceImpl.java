package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.repository.EmailCampaignFileRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class EmailCampaignFileServiceImpl
        implements BaseFileService<EmailCampaignType, EmailCampaignActivity, EmailCampaignFile> {

    @Autowired
    private EmailCampaignFileRepository repository;


    @Override
    public List<EmailCampaignFile> getByActivity(
            final EmailCampaignActivity emailCampaignActivity
    ) {
        return repository.findByEmailCampaignActivity(emailCampaignActivity);
    }

    @Override
    public Optional<EmailCampaignFile> getByNameAndActivity(
            final String filename,
            final EmailCampaignActivity emailCampaignActivity
    ) {
        return repository.findByNameIsIgnoreCaseAndEmailCampaignActivity(filename, emailCampaignActivity);
    }

    @Override
    public EmailCampaignFile save(
            final EmailCampaignFile emailCampaignFile
    ) {
        return repository.save(emailCampaignFile);
    }

    @Override
    public void delete(
            final EmailCampaignFile emailCampaignFile
    ) {
        repository.delete(emailCampaignFile);
    }

}
