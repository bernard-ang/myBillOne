package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.repository.WhatsAppActivityRepository;
import com.grabbill.core.service.BaseActivityService;
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
 * @author seez
 */
public class WhatsAppActivityServiceImpl
        implements BaseActivityService<WhatsAppType, WhatsAppActivity> {

    @Autowired
    private WhatsAppActivityRepository repository;


    @Override
    public Page<WhatsAppActivity> getByType(
            final WhatsAppType whatsAppType,
            final Pageable pageable
    ) {
        return repository.findByWhatsAppType(whatsAppType, pageable);
    }

    @Override
    public Page<WhatsAppActivity> getByTypeAndName(WhatsAppType type, String name, Pageable pageable) {
        return repository.findByWhatsAppTypeAndNameIsContainingIgnoreCase(type, name, pageable);
    }

    @Override
    public Page<WhatsAppActivity> getByTypeAndStatus(WhatsAppType type, ProcessStatus status, Pageable pageable) {
        return repository.findByWhatsAppTypeAndStatus(type, status, pageable);
    }

    @Override
    public Page<WhatsAppActivity> getByTypeAndNameAndStatus(WhatsAppType type, String name, ProcessStatus status, Pageable pageable) {
        return repository.findByWhatsAppTypeAndNameIsContainingIgnoreCaseAndStatus(type, name, status, pageable);
    }

    @Override
    public List<WhatsAppActivity> getByType(
            final WhatsAppType whatsAppType
    ) {
        return repository.findByWhatsAppType(whatsAppType);
    }

    @Override
    public List<WhatsAppActivity> getByTypeAndStatusIn(
            final WhatsAppType whatsAppType,
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findByWhatsAppTypeAndStatusIn(whatsAppType, processStatuses);
    }

    @Override
    public Optional<WhatsAppActivity> getById(
            final Long id
    ) {
        return repository.findById(id);
    }

    @Override
    public Optional<WhatsAppActivity> getByIdAndType(
            final Long id,
            final WhatsAppType whatsAppType
    ) {
        return repository.findByIdAndWhatsAppType(id, whatsAppType);
    }

    @Override
    public Optional<WhatsAppActivity> getByNameAndType(
            final String name,
            final WhatsAppType whatsAppType
    ) {
        return repository.findByNameAndWhatsAppType(name, whatsAppType);
    }

    @Override
    public List<WhatsAppActivity> getAllByStatusInAndNotPurged(
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findAllByStatusInAndPurgedTimestampIsNull(processStatuses);
    }

    @Override
    public WhatsAppActivity save(
            final WhatsAppActivity whatsAppActivity
    ) {
        return repository.save(whatsAppActivity);
    }

    @Override
    public WhatsAppActivity saveAndFlush(
            final WhatsAppActivity whatsAppActivity
    ) {
        return repository.saveAndFlush(whatsAppActivity);
    }

    @Override
    public void delete(
            final WhatsAppActivity whatsAppActivity
    ) {
        repository.delete(whatsAppActivity);
    }

    @Override
    public int countAllSubmittedActivities() {
        return repository.countAllBySubmittedTimestampIsNotNull();
    }

    @Override
    public List<WhatsAppActivity> getAllProcessedActivities() {
        return repository.findAllByProcessedTimestampIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public WhatsAppActivity markAsProcessing(final Long id) {
        WhatsAppActivity targetActivity = repository.findById(id).get();
        targetActivity.setStatus(ProcessStatus.PROCESSING);
        targetActivity.setProcessingTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return saveAndFlush(targetActivity);
    }

    @Override
    public List<WhatsAppActivity> getAllByAccountBetween(
            final Account account,
            final OffsetDateTime start,
            final OffsetDateTime end
    ) {
        throw new UnsupportedOperationException();
    }

}
