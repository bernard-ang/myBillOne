package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppIndexRow;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author michaellow
 */
public interface WhatsAppIndexRowRepository
        extends JpaRepository<WhatsAppIndexRow, Long>, WhatsAppIndexRowRepositoryCustom {
}
