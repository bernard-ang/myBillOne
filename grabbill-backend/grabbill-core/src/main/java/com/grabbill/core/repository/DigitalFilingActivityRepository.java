package com.grabbill.core.repository;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingType;
import com.grabbill.core.model.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface DigitalFilingActivityRepository extends JpaRepository<DigitalFilingActivity, Long> {

    Page<DigitalFilingActivity> findByDigitalFilingType(DigitalFilingType digitalFilingType, Pageable pageable);

    Page<DigitalFilingActivity> findByDigitalFilingTypeAndNameIsContainingIgnoreCase(DigitalFilingType digitalFilingType, String name, Pageable pageable);

    Page<DigitalFilingActivity> findByDigitalFilingTypeAndStatus(DigitalFilingType digitalFilingType, ProcessStatus status, Pageable pageable);

    Page<DigitalFilingActivity> findByDigitalFilingTypeAndNameIsContainingIgnoreCaseAndStatus(DigitalFilingType digitalFilingType, String name, ProcessStatus status, Pageable pageable);

    List<DigitalFilingActivity> findByDigitalFilingType(DigitalFilingType digitalFilingType);

    List<DigitalFilingActivity> findByDigitalFilingTypeAndStatusIn(DigitalFilingType digitalFilingType, List<ProcessStatus> statuses);

    Optional<DigitalFilingActivity> findByNameAndDigitalFilingType(String name, DigitalFilingType digitalFilingType);

    Optional<DigitalFilingActivity> findByIdAndDigitalFilingType(Long id, DigitalFilingType digitalFilingType);

    List<DigitalFilingActivity> findAllByStatusInAndPurgedTimestampIsNull(List<ProcessStatus> statuses);

    int countAllBySubmittedTimestampIsNotNull();

    List<DigitalFilingActivity> findAllByProcessedTimestampIsNotNull();

}
