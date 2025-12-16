package com.grabbill.core.repository;

import com.grabbill.core.entity.SmsActivity;
import com.grabbill.core.entity.SmsType;
import com.grabbill.core.model.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface SmsActivityRepository extends JpaRepository<SmsActivity, Long> {

    Page<SmsActivity> findBySmsType(SmsType smsType, Pageable pageable);

    Page<SmsActivity> findBySmsTypeAndNameIsContainingIgnoreCase(SmsType smsType, String name, Pageable pageable);

    Page<SmsActivity> findBySmsTypeAndStatus(SmsType smsType, ProcessStatus status, Pageable pageable);

    Page<SmsActivity> findBySmsTypeAndNameIsContainingIgnoreCaseAndStatus(SmsType smsType, String name, ProcessStatus status, Pageable pageable);

    List<SmsActivity> findBySmsType(SmsType smsType);

    List<SmsActivity> findBySmsTypeAndStatusIn(SmsType smsType, List<ProcessStatus> statuses);

    Optional<SmsActivity> findByNameAndSmsType(String name, SmsType smsType);

    Optional<SmsActivity> findByIdAndSmsType(Long id, SmsType smsType);

    int countAllBySubmittedTimestampIsNotNull();

    List<SmsActivity> findAllByProcessedTimestampIsNotNull();

    List<SmsActivity> findBySmsTypeIsInAndProcessedTimestampBetween(List<SmsType> smsTypes, OffsetDateTime start, OffsetDateTime end);

}
