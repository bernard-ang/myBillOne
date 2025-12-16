package com.grabbill.core.repository;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppFile;
import com.grabbill.core.entity.MTWhatsAppType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTWhatsAppFileRepository extends JpaRepository<MTWhatsAppFile, Long> {

    Page<MTWhatsAppFile> findByMtWhatsAppTypeAndMtWhatsAppIndexRowsIsNotEmpty(
            MTWhatsAppType mtWhatsAppType,
            Pageable pageable
    );

    Page<MTWhatsAppFile> findByNameIsContainingIgnoreCaseAndMtWhatsAppTypeAndMtWhatsAppIndexRowsIsNotEmpty(
            String filename,
            MTWhatsAppType mtWhatsAppType,
            Pageable pageable
    );

    List<MTWhatsAppFile> findByMtWhatsAppActivity(
            MTWhatsAppActivity mtWhatsAppActivity
    );

    Optional<MTWhatsAppFile> findByNameIsIgnoreCaseAndMtWhatsAppActivity(
            String filename,
            MTWhatsAppActivity mtWhatsAppActivity
    );

    Long countByMtWhatsAppActivityIn(
            List<MTWhatsAppActivity> mtWhatsAppActivity
    );
}
