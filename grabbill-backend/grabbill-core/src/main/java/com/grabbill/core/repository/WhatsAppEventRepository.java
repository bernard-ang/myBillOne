package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author seez
 */
public interface WhatsAppEventRepository extends JpaRepository<WhatsAppEvent, Long>, WhatsAppEventRepositoryCustom {

    List<WhatsAppEvent> findAllByProcessedIsFalseAndProcessedCountLessThan(int total);
}
