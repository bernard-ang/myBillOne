package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppFile;
import com.grabbill.core.entity.WhatsAppType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public interface WhatsAppFileRepository extends JpaRepository<WhatsAppFile, Long> {

    Page<WhatsAppFile> findByWhatsAppTypeAndWhatsAppIndexRowsIsNotEmpty(
            WhatsAppType whatsAppType,
            Pageable pageable
    );

    Page<WhatsAppFile> findByNameIsContainingIgnoreCaseAndWhatsAppTypeAndWhatsAppIndexRowsIsNotEmpty(
            String filename,
            WhatsAppType whatsAppType,
            Pageable pageable
    );

    List<WhatsAppFile> findByWhatsAppActivity(
            WhatsAppActivity whatsAppActivity
    );

    Optional<WhatsAppFile> findByNameIsIgnoreCaseAndWhatsAppActivity(
            String filename,
            WhatsAppActivity whatsAppActivity
    );

}
