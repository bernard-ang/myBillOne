package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppType;
import com.grabbill.core.model.ProcessStatus;
import com.grabbill.core.repository.MTWhatsAppActivityRepository;
import com.grabbill.core.repository.MTWhatsAppRecordRepository;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MTWhatsAppActivityServiceImpl
        implements BaseActivityService<MTWhatsAppType, MTWhatsAppActivity> {

    @Autowired
    private MTWhatsAppActivityRepository repository;

    @Autowired private MTWhatsAppRecordRepository recordRepository;


    @Override
    public Page<MTWhatsAppActivity> getByType(
            final MTWhatsAppType mtWhatsAppType,
            final Pageable pageable
    ) {
        return repository.findByMtWhatsAppType(mtWhatsAppType, pageable);
    }

    @Override
    public Page<MTWhatsAppActivity> getByTypeAndName(MTWhatsAppType type, String name, Pageable pageable) {
        return repository.findByMtWhatsAppTypeAndNameIsContainingIgnoreCase(type, name, pageable);
    }

    @Override
    public Page<MTWhatsAppActivity> getByTypeAndStatus(MTWhatsAppType mtWhatsAppType, ProcessStatus status, Pageable pageable) {
        return repository.findByMtWhatsAppTypeAndStatus(mtWhatsAppType, status, pageable);
    }

    @Override
    public Page<MTWhatsAppActivity> getByTypeAndNameAndStatus(MTWhatsAppType mtWhatsAppType, String name, ProcessStatus status, Pageable pageable) {
        return repository.findByMtWhatsAppTypeAndNameIsContainingIgnoreCaseAndStatus(mtWhatsAppType, name, status, pageable);
    }

    @Override
    public List<MTWhatsAppActivity> getByType(
            final MTWhatsAppType mtWhatsAppType
    ) {
        return repository.findByMtWhatsAppType(mtWhatsAppType);
    }

    @Override
    public List<MTWhatsAppActivity> getByTypeAndStatusIn(
            final MTWhatsAppType mtWhatsAppType,
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findByMtWhatsAppTypeAndStatusIn(mtWhatsAppType, processStatuses);
    }

    @Override
    public Optional<MTWhatsAppActivity> getById(
            final Long id
    ) {
        return repository.findById(id);
    }

    @Override
    public Optional<MTWhatsAppActivity> getByIdAndType(
            final Long id,
            final MTWhatsAppType mtWhatsAppType
    ) {
        return repository.findByIdAndMtWhatsAppType(id, mtWhatsAppType);
    }

    @Override
    public Optional<MTWhatsAppActivity> getByNameAndType(
            final String name,
            final MTWhatsAppType mtWhatsAppType
    ) {
        return repository.findByNameAndMtWhatsAppType(name, mtWhatsAppType);
    }

    @Override
    public List<MTWhatsAppActivity> getAllByStatusInAndNotPurged(
            final List<ProcessStatus> processStatuses
    ) {
        return repository.findAllByStatusInAndPurgedTimestampIsNull(processStatuses);
    }

    @Override
    public MTWhatsAppActivity save(
            final MTWhatsAppActivity mtWhatsAppActivity
    ) {
        return repository.save(mtWhatsAppActivity);
    }

    @Override
    public MTWhatsAppActivity saveAndFlush(
            final MTWhatsAppActivity mtWhatsAppActivity
    ) {
        return repository.saveAndFlush(mtWhatsAppActivity);
    }

    @Override
    public void delete(
            final MTWhatsAppActivity mtWhatsAppActivity
    ) {
        repository.delete(mtWhatsAppActivity);
    }

    @Override
    public int countAllSubmittedActivities() {
        return repository.countAllBySubmittedTimestampIsNotNull();
    }

    @Override
    public List<MTWhatsAppActivity> getAllProcessedActivities() {
        return repository.findAllByProcessedTimestampIsNotNull();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public MTWhatsAppActivity markAsProcessing(final Long id) {
        MTWhatsAppActivity targetActivity = repository.findById(id).get();
        targetActivity.setStatus(ProcessStatus.PROCESSING);
        targetActivity.setProcessingTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return saveAndFlush(targetActivity);
    }

    @Override
    public List<MTWhatsAppActivity> getAllByAccountBetween(
            final Account account,
            final OffsetDateTime start,
            final OffsetDateTime end
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MTWhatsAppActivity updateCount(MTWhatsAppActivity activity) {
        activity.setWhatsAppStatusSent((int)recordRepository.countByWhatsAppStatusSentTrueAndMtWhatsAppActivity(activity));

        activity.setWhatsAppStatusDelivered((int)recordRepository.countByWhatsAppStatusDeliveredTrueAndMtWhatsAppActivity(activity));
        activity.setWhatsAppStatusSkip((int)recordRepository.countByWhatsAppStatusSkipTrueAndMtWhatsAppActivity(activity));
        activity.setWhatsAppStatusAcknowledge((int)recordRepository.countByWhatsAppStatusAcknowledgeTrueAndMtWhatsAppActivity(activity));
        activity.setWhatsAppStatusRead((int)recordRepository.countByWhatsAppStatusReadTrueAndMtWhatsAppActivity(activity));
        activity.setWhatsAppStatusFailed((int)recordRepository.countByWhatsAppStatusFailedTrueAndMtWhatsAppActivity(activity));

        log.info("Updated MT WA activity {} count with sent {}, delivered {}, skip {}, acknowledge {}, read {}, failed {}",
                activity.getId(),
                activity.getWhatsAppStatusSent(), activity.getWhatsAppStatusDelivered(),
                activity.getWhatsAppStatusSkip(), activity.getWhatsAppStatusAcknowledge(),
                activity.getWhatsAppStatusRead(), activity.getWhatsAppStatusFailed()
        );
        return activity;
    }
}
