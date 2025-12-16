package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.repository.TransactionalEmailActivityRepository;
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
public class TransactionalEmailActivityServiceImpl
        implements BaseActivityService<TransactionalEmailType, TransactionalEmailActivity> {

    @Autowired
    private TransactionalEmailActivityRepository repository;


    @Override
    public Page<TransactionalEmailActivity> getByType(
            final TransactionalEmailType transactionalEmailType,
            final Pageable pageable
    ) {
        return repository.findByTransactionalEmailType(transactionalEmailType, pageable);
    }

    @Override
    public Page<TransactionalEmailActivity> getByTypeAndName(TransactionalEmailType type, String name, Pageable pageable) {
        return repository.findByTransactionalEmailTypeAndNameIsContainingIgnoreCase(type, name, pageable);
    }

    @Override
    public Page<TransactionalEmailActivity> getByTypeAndStatus(TransactionalEmailType type, ProcessStatus status, Pageable pageable) {
        return repository.findByTransactionalEmailTypeAndStatus(type, status, pageable);
    }

    @Override
    public Page<TransactionalEmailActivity> getByTypeAndNameAndStatus(TransactionalEmailType type, String name, ProcessStatus status, Pageable pageable) {
        return repository.findByTransactionalEmailTypeAndNameIsContainingIgnoreCaseAndStatus(type, name, status, pageable);
    }

    @Override
    public List<TransactionalEmailActivity> getByType(
            final TransactionalEmailType transactionalEmailType
    ) {
        return repository.findByTransactionalEmailType(transactionalEmailType);
    }

    @Override
    public List<TransactionalEmailActivity> getByTypeAndStatusIn(
            final TransactionalEmailType transactionalEmailType,
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findByTransactionalEmailTypeAndStatusIn(transactionalEmailType, processStatuses);
    }

    @Override
    public Optional<TransactionalEmailActivity> getById(
            final Long id
    ) {
        return repository.findById(id);
    }

    @Override
    public Optional<TransactionalEmailActivity> getByIdAndType(
            final Long id,
            final TransactionalEmailType transactionalEmailType
    ) {
        return repository.findByIdAndTransactionalEmailType(id, transactionalEmailType);
    }

    @Override
    public Optional<TransactionalEmailActivity> getByNameAndType(
            final String name,
            final TransactionalEmailType transactionalEmailType
    ) {
        return repository.findByNameAndTransactionalEmailType(name, transactionalEmailType);
    }

    @Override
    public List<TransactionalEmailActivity> getAllByStatusInAndNotPurged(
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findAllByStatusInAndPurgedTimestampIsNull(processStatuses);
    }

    @Override
    public TransactionalEmailActivity save(
            final TransactionalEmailActivity transactionalEmailActivity
    ) {
        return repository.save(transactionalEmailActivity);
    }

    @Override
    public TransactionalEmailActivity saveAndFlush(
            final TransactionalEmailActivity transactionalEmailActivity
    ) {
        return repository.saveAndFlush(transactionalEmailActivity);
    }

    @Override
    public void delete(
            final TransactionalEmailActivity transactionalEmailActivity
    ) {
        repository.delete(transactionalEmailActivity);
    }

    @Override
    public int countAllSubmittedActivities() {
        return repository.countAllBySubmittedTimestampIsNotNull();
    }

    @Override
    public List<TransactionalEmailActivity> getAllProcessedActivities() {
        return repository.findAllByProcessedTimestampIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public TransactionalEmailActivity markAsProcessing(final Long id) {
        TransactionalEmailActivity targetActivity = repository.findById(id).get();
        targetActivity.setStatus(ProcessStatus.PROCESSING);
        targetActivity.setProcessingTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return saveAndFlush(targetActivity);
    }

    @Override
    public List<TransactionalEmailActivity> getAllByAccountBetween(
            final Account account,
            final OffsetDateTime start,
            final OffsetDateTime end
    ) {
        throw new UnsupportedOperationException();
    }

}
