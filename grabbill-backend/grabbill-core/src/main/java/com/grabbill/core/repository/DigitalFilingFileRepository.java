package com.grabbill.core.repository;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingFile;
import com.grabbill.core.entity.DigitalFilingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface DigitalFilingFileRepository extends JpaRepository<DigitalFilingFile, Long> {

    Page<DigitalFilingFile> findByDigitalFilingTypeAndDigitalFilingIndexRowsIsNotEmpty(
            DigitalFilingType digitalFilingType,
            Pageable pageable
    );

    Page<DigitalFilingFile> findByNameIsContainingIgnoreCaseAndDigitalFilingTypeAndDigitalFilingIndexRowsIsNotEmpty(
            String filename,
            DigitalFilingType digitalFilingType,
            Pageable pageable
    );

    List<DigitalFilingFile> findByDigitalFilingActivity(
            DigitalFilingActivity digitalFilingActivity
    );

    Optional<DigitalFilingFile> findByNameIsIgnoreCaseAndDigitalFilingActivity(
            String filename,
            DigitalFilingActivity digitalFilingActivity
    );

}
