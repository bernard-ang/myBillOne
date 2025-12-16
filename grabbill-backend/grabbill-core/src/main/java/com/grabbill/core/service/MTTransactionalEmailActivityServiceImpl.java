package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.MTTransactionalEmailActivity;
import com.grabbill.core.entity.MTTransactionalEmailType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.repository.MTTransactionalEmailActivityRepository;
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
public class MTTransactionalEmailActivityServiceImpl
        implements BaseActivityService<MTTransactionalEmailType, MTTransactionalEmailActivity> {

    @Autowired
    private MTTransactionalEmailActivityRepository repository;


    @Override
    public Page<MTTransactionalEmailActivity> getByType(
            final MTTransactionalEmailType mtTransactionalEmailType,
            final Pageable pageable
    ) {
        return repository.findByMtTransactionalEmailType(mtTransactionalEmailType, pageable);
    }

    @Override
    public Page<MTTransactionalEmailActivity> getByTypeAndName(MTTransactionalEmailType type, String name, Pageable pageable) {
        return repository.findByMtTransactionalEmailTypeAndNameIsContainingIgnoreCase(type, name, pageable);
    }

    @Override
    public Page<MTTransactionalEmailActivity> getByTypeAndStatus(MTTransactionalEmailType type, ProcessStatus status, Pageable pageable) {
        return repository.findByMtTransactionalEmailTypeAndStatus(type, status, pageable);
    }

    @Override
    public Page<MTTransactionalEmailActivity> getByTypeAndNameAndStatus(MTTransactionalEmailType type, String name, ProcessStatus status, Pageable pageable) {
        return repository.findByMtTransactionalEmailTypeAndNameIsContainingIgnoreCaseAndStatus(type, name, status, pageable);
    }

    @Override
    public List<MTTransactionalEmailActivity> getByType(
            final MTTransactionalEmailType mtTransactionalEmailType
    ) {
        return repository.findByMtTransactionalEmailType(mtTransactionalEmailType);
    }

    @Override
    public List<MTTransactionalEmailActivity> getByTypeAndStatusIn(
            final MTTransactionalEmailType mtTransactionalEmailType,
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findByMtTransactionalEmailTypeAndStatusIn(mtTransactionalEmailType, processStatuses);
    }

    @Override
    public Optional<MTTransactionalEmailActivity> getById(
            final Long id
    ) {
        return repository.findById(id);
    }

    @Override
    public Optional<MTTransactionalEmailActivity> getByIdAndType(
            final Long id,
            final MTTransactionalEmailType mtTransactionalEmailType
    ) {
        return repository.findByIdAndMtTransactionalEmailType(id, mtTransactionalEmailType);
    }

    @Override
    public Optional<MTTransactionalEmailActivity> getByNameAndType(
            final String name,
            final MTTransactionalEmailType mtTransactionalEmailType
    ) {
        return repository.findByNameAndMtTransactionalEmailType(name, mtTransactionalEmailType);
    }

    @Override
    public List<MTTransactionalEmailActivity> getAllByStatusInAndNotPurged(
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findAllByStatusInAndPurgedTimestampIsNull(processStatuses);
    }

    @Override
    public MTTransactionalEmailActivity save(
            final MTTransactionalEmailActivity mtTransactionalEmailActivity
    ) {
        return repository.save(mtTransactionalEmailActivity);
    }

    @Override
    public MTTransactionalEmailActivity saveAndFlush(
            final MTTransactionalEmailActivity mtTransactionalEmailActivity
    ) {
        return repository.saveAndFlush(mtTransactionalEmailActivity);
    }

    @Override
    public void delete(
            final MTTransactionalEmailActivity mtTransactionalEmailActivity
    ) {
        repository.delete(mtTransactionalEmailActivity);
    }

    @Override
    public int countAllSubmittedActivities() {
        return repository.countAllBySubmittedTimestampIsNotNull();
    }

    @Override
    public List<MTTransactionalEmailActivity> getAllProcessedActivities() {
        return repository.findAllByProcessedTimestampIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public MTTransactionalEmailActivity markAsProcessing(final Long id) {
        MTTransactionalEmailActivity targetActivity = repository.findById(id).get();
        targetActivity.setStatus(ProcessStatus.PROCESSING);
        targetActivity.setProcessingTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return saveAndFlush(targetActivity);
    }

    @Override
    public List<MTTransactionalEmailActivity> getAllByAccountBetween(
            final Account account,
            final OffsetDateTime start,
            final OffsetDateTime end
    ) {
        throw new UnsupportedOperationException();
    }

}
