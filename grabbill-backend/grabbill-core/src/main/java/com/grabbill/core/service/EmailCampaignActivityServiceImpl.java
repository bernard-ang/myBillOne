package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.EmailCampaignActivity;
import com.grabbill.core.entity.EmailCampaignType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.repository.EmailCampaignActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class EmailCampaignActivityServiceImpl
        implements BaseActivityService<EmailCampaignType, EmailCampaignActivity> {

    @Autowired
    private EmailCampaignActivityRepository repository;


    @Override
    public Page<EmailCampaignActivity> getByType(
            final EmailCampaignType emailCampaignType,
            final Pageable pageable
    ) {
        return repository.findByEmailCampaignType(emailCampaignType, pageable);
    }

    @Override
    public Page<EmailCampaignActivity> getByTypeAndName(EmailCampaignType type, String name, Pageable pageable) {
        return repository.findByEmailCampaignTypeAndNameIsContainingIgnoreCase(type, name, pageable);
    }

    @Override
    public Page<EmailCampaignActivity> getByTypeAndStatus(EmailCampaignType type, ProcessStatus status, Pageable pageable) {
        return repository.findByEmailCampaignTypeAndStatus(type, status, pageable);
    }

    @Override
    public Page<EmailCampaignActivity> getByTypeAndNameAndStatus(EmailCampaignType type, String name, ProcessStatus status, Pageable pageable) {
        return repository.findByEmailCampaignTypeAndNameIsContainingIgnoreCaseAndStatus(type, name, status, pageable);
    }

    @Override
    public List<EmailCampaignActivity> getByType(
            final EmailCampaignType emailCampaignType
    ) {
        return repository.findByEmailCampaignType(emailCampaignType);
    }

    @Override
    public List<EmailCampaignActivity> getByTypeAndStatusIn(
            final EmailCampaignType emailCampaignType,
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findByEmailCampaignTypeAndStatusIn(emailCampaignType, processStatuses);
    }

    @Override
    public Optional<EmailCampaignActivity> getById(
            final Long id
    ) {
        return repository.findById(id);
    }

    @Override
    public Optional<EmailCampaignActivity> getByIdAndType(
            final Long id,
            final EmailCampaignType emailCampaignType
    ) {
        return repository.findByIdAndEmailCampaignType(id, emailCampaignType);
    }

    @Override
    public Optional<EmailCampaignActivity> getByNameAndType(
            final String name,
            final EmailCampaignType emailCampaignType
    ) {
        return repository.findByNameAndEmailCampaignType(name, emailCampaignType);
    }

    @Override
    public List<EmailCampaignActivity> getAllByStatusInAndNotPurged(
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findAllByStatusInAndPurgedTimestampIsNull(processStatuses);
    }

    @Override
    public EmailCampaignActivity save(
            final EmailCampaignActivity emailCampaignActivity
    ) {
        return repository.save(emailCampaignActivity);
    }

    @Override
    public EmailCampaignActivity saveAndFlush(
            final EmailCampaignActivity emailCampaignActivity
    ) {
        return repository.saveAndFlush(emailCampaignActivity);
    }

    @Override
    public void delete(
            final EmailCampaignActivity emailCampaignActivity
    ) {
        repository.delete(emailCampaignActivity);
    }

    @Override
    public int countAllSubmittedActivities() {
        return repository.countAllBySubmittedTimestampIsNotNull();
    }

    @Override
    public List<EmailCampaignActivity> getAllProcessedActivities() {
        return repository.findAllByProcessedTimestampIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public EmailCampaignActivity markAsProcessing(final Long id) {
        EmailCampaignActivity targetActivity = repository.findById(id).get();
        targetActivity.setStatus(ProcessStatus.PROCESSING);
        targetActivity.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return saveAndFlush(targetActivity);
    }

    @Override
    public List<EmailCampaignActivity> getAllByAccountBetween(
            final Account account,
            final OffsetDateTime start,
            final OffsetDateTime end
    ) {
        // TODO
        throw new UnsupportedOperationException();
    }

}
