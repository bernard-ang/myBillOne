package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppType;
import com.grabbill.core.model.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public interface WhatsAppActivityRepository extends JpaRepository<WhatsAppActivity, Long> {

    Page<WhatsAppActivity> findByWhatsAppType(WhatsAppType whatsAppType, Pageable pageable);

    Page<WhatsAppActivity> findByWhatsAppTypeAndNameIsContainingIgnoreCase(WhatsAppType whatsAppType, String name, Pageable pageable);

    Page<WhatsAppActivity> findByWhatsAppTypeAndStatus(WhatsAppType whatsAppType, ProcessStatus status, Pageable pageable);

    Page<WhatsAppActivity> findByWhatsAppTypeAndNameIsContainingIgnoreCaseAndStatus(WhatsAppType whatsAppType, String name, ProcessStatus status, Pageable pageable);

    List<WhatsAppActivity> findByWhatsAppType(WhatsAppType whatsAppType);

    List<WhatsAppActivity> findByWhatsAppTypeAndStatusIn(WhatsAppType whatsAppType, List<ProcessStatus> statuses);

    Optional<WhatsAppActivity> findByNameAndWhatsAppType(String name, WhatsAppType whatsAppType);

    Optional<WhatsAppActivity> findByIdAndWhatsAppType(Long id, WhatsAppType whatsAppType);

    List<WhatsAppActivity> findAllByStatusInAndPurgedTimestampIsNull(List<ProcessStatus> statuses);

    int countAllBySubmittedTimestampIsNotNull();

    List<WhatsAppActivity> findAllByProcessedTimestampIsNotNull();

}
