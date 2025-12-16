package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.repository.DigitalFilingActivityRepository;
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
public class DigitalFilingActivityServiceImpl
        implements BaseActivityService<DigitalFilingType, DigitalFilingActivity> {

    @Autowired
    private DigitalFilingActivityRepository repository;


    @Override
    public Page<DigitalFilingActivity> getByType(
            final DigitalFilingType digitalFilingType,
            final Pageable pageable
    ) {
        return repository.findByDigitalFilingType(digitalFilingType, pageable);
    }

    @Override
    public Page<DigitalFilingActivity> getByTypeAndName(DigitalFilingType type, String name, Pageable pageable) {
        return repository.findByDigitalFilingTypeAndNameIsContainingIgnoreCase(type, name, pageable);
    }

    @Override
    public Page<DigitalFilingActivity> getByTypeAndStatus(DigitalFilingType type, ProcessStatus status, Pageable pageable) {
        return repository.findByDigitalFilingTypeAndStatus(type, status, pageable);
    }

    @Override
    public Page<DigitalFilingActivity> getByTypeAndNameAndStatus(DigitalFilingType type, String name, ProcessStatus status, Pageable pageable) {
        return repository.findByDigitalFilingTypeAndNameIsContainingIgnoreCaseAndStatus(type, name, status, pageable);
    }

    @Override
    public List<DigitalFilingActivity> getByType(
            final DigitalFilingType digitalFilingType
    ) {
        return repository.findByDigitalFilingType(digitalFilingType);
    }

    @Override
    public List<DigitalFilingActivity> getByTypeAndStatusIn(
            final DigitalFilingType digitalFilingType,
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findByDigitalFilingTypeAndStatusIn(digitalFilingType, processStatuses);
    }

    @Override
    public Optional<DigitalFilingActivity> getById(
            final Long id
    ) {
        return repository.findById(id);
    }

    @Override
    public Optional<DigitalFilingActivity> getByIdAndType(
            final Long id,
            final DigitalFilingType digitalFilingType
    ) {
        return repository.findByIdAndDigitalFilingType(id, digitalFilingType);
    }

    @Override
    public Optional<DigitalFilingActivity> getByNameAndType(String name, DigitalFilingType digitalFilingType) {
        return repository.findByNameAndDigitalFilingType(name, digitalFilingType);
    }

    @Override
    public List<DigitalFilingActivity> getAllByStatusInAndNotPurged(
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findAllByStatusInAndPurgedTimestampIsNull(processStatuses);
    }

    @Override
    public DigitalFilingActivity save(
            final DigitalFilingActivity digitalFilingActivity
    ) {
        return repository.save(digitalFilingActivity);
    }

    @Override
    public DigitalFilingActivity saveAndFlush(
            final DigitalFilingActivity digitalFilingActivity
    ) {
        return repository.saveAndFlush(digitalFilingActivity);
    }

    @Override
    public void delete(
            final DigitalFilingActivity digitalFilingActivity
    ) {
        repository.delete(digitalFilingActivity);
    }

    @Override
    public int countAllSubmittedActivities() {
        return repository.countAllBySubmittedTimestampIsNotNull();
    }

    @Override
    public List<DigitalFilingActivity> getAllProcessedActivities() {
        return repository.findAllByProcessedTimestampIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public DigitalFilingActivity markAsProcessing(final Long id) {
        DigitalFilingActivity targetActivity = repository.findById(id).get();
        targetActivity.setStatus(ProcessStatus.PROCESSING);
        targetActivity.setProcessingTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return saveAndFlush(targetActivity);
    }

    @Override
    public List<DigitalFilingActivity> getAllByAccountBetween(
            final Account account,
            final OffsetDateTime start,
            final OffsetDateTime end
    ) {
        // TODO
        throw new UnsupportedOperationException();
    }

}
