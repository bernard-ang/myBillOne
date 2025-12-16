package com.grabbill.core.repository;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppIndexRow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author michaellow
 */
public interface MTWhatsAppIndexRowRepository
        extends JpaRepository<MTWhatsAppIndexRow, Long>, MTWhatsAppIndexRowRepositoryCustom {

    Long countByMtWhatsAppActivityIn(List<MTWhatsAppActivity> mtWhatsAppActivities);
}
