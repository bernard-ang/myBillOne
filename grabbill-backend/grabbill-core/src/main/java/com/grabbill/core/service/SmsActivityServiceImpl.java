package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.SmsActivity;
import com.grabbill.core.entity.SmsType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.repository.SmsActivityRepository;
import com.grabbill.core.repository.SmsTypeRepository;
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
public class SmsActivityServiceImpl
        implements BaseActivityService<SmsType, SmsActivity> {

    @Autowired
    private SmsActivityRepository repository;

    @Autowired
    private SmsTypeRepository smsTypeRepository;


    @Override
    public Page<SmsActivity> getByType(
            final SmsType smsType,
            final Pageable pageable
    ) {
        return repository.findBySmsType(smsType, pageable);
    }

    @Override
    public Page<SmsActivity> getByTypeAndName(SmsType type, String name, Pageable pageable) {
        return repository.findBySmsTypeAndNameIsContainingIgnoreCase(type, name, pageable);
    }

    @Override
    public Page<SmsActivity> getByTypeAndStatus(SmsType type, ProcessStatus status, Pageable pageable) {
        return repository.findBySmsTypeAndStatus(type, status, pageable);
    }

    @Override
    public Page<SmsActivity> getByTypeAndNameAndStatus(SmsType type, String name, ProcessStatus status, Pageable pageable) {
        return repository.findBySmsTypeAndNameIsContainingIgnoreCaseAndStatus(type, name, status, pageable);
    }

    @Override
    public List<SmsActivity> getByType(final SmsType type) {
        return repository.findBySmsType(type);
    }

    @Override
    public List<SmsActivity> getByTypeAndStatusIn(
            final SmsType smsType,
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findBySmsTypeAndStatusIn(smsType, processStatuses);
    }

    @Override
    public Optional<SmsActivity> getById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<SmsActivity> getByIdAndType(
            final Long id,
            final SmsType smsType
    ) {
        return repository.findByIdAndSmsType(id, smsType);
    }

    @Override
    public Optional<SmsActivity> getByNameAndType(
            final String name,
            final SmsType smsType
    ) {
        return repository.findByNameAndSmsType(name, smsType);
    }

    @Override
    public List<SmsActivity> getAllByStatusInAndNotPurged(
            final List<ProcessStatus> processStatuses
    ) {
        throw new UnsupportedOperationException("getAllByStatusInAndNotPurged is not supported to sms type");
    }

    @Override
    public SmsActivity save(
            final SmsActivity smsActivity
    ) {
        return repository.save(smsActivity);
    }

    @Override
    public SmsActivity saveAndFlush(
            final SmsActivity smsActivity
    ) {
        return repository.saveAndFlush(smsActivity);
    }

    @Override
    public void delete(
            final SmsActivity smsActivity
    ) {
        repository.delete(smsActivity);
    }

    @Override
    public int countAllSubmittedActivities() {
        return repository.countAllBySubmittedTimestampIsNotNull();
    }

    @Override
    public List<SmsActivity> getAllProcessedActivities() {
        return repository.findAllByProcessedTimestampIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public SmsActivity markAsProcessing(final Long id) {
        SmsActivity targetActivity = repository.findById(id).get();
        targetActivity.setStatus(ProcessStatus.PROCESSING);
        targetActivity.setProcessedTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return saveAndFlush(targetActivity);
    }

    @Override
    public List<SmsActivity> getAllByAccountBetween(
            final Account account,
            final OffsetDateTime start,
            final OffsetDateTime end
    ) {
        List<SmsType> types = smsTypeRepository.findByAccountId(account.getId());
        return repository.findBySmsTypeIsInAndProcessedTimestampBetween(types, start, end);
    }
}
