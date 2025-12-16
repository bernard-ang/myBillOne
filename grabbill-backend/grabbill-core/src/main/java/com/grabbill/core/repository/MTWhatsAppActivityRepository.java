package com.grabbill.core.repository;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppType;
import com.grabbill.core.model.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTWhatsAppActivityRepository extends JpaRepository<MTWhatsAppActivity, Long> {

    Page<MTWhatsAppActivity> findByMtWhatsAppType(MTWhatsAppType mtWhatsAppType, Pageable pageable);

    Page<MTWhatsAppActivity> findByMtWhatsAppTypeAndNameIsContainingIgnoreCase(MTWhatsAppType mtWhatsAppType, String name, Pageable pageable);

    Page<MTWhatsAppActivity> findByMtWhatsAppTypeAndStatus(MTWhatsAppType mtWhatsAppType, ProcessStatus status, Pageable pageable);

    Page<MTWhatsAppActivity> findByMtWhatsAppTypeAndNameIsContainingIgnoreCaseAndStatus(MTWhatsAppType mtWhatsAppType, String name, ProcessStatus status, Pageable pageable);

    List<MTWhatsAppActivity> findByMtWhatsAppType(MTWhatsAppType mtWhatsAppType);

    List<MTWhatsAppActivity> findByMtWhatsAppTypeAndStatusIn(MTWhatsAppType mtWhatsAppType, List<ProcessStatus> statuses);

    Optional<MTWhatsAppActivity> findByNameAndMtWhatsAppType(String name, MTWhatsAppType mtWhatsAppType);

    Optional<MTWhatsAppActivity> findByIdAndMtWhatsAppType(Long id, MTWhatsAppType mtWhatsAppType);

    List<MTWhatsAppActivity> findAllByStatusInAndPurgedTimestampIsNull(List<ProcessStatus> statuses);

    int countAllBySubmittedTimestampIsNotNull();

    List<MTWhatsAppActivity> findAllByProcessedTimestampIsNotNull();

}
