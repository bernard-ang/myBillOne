package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author seez
 */
public interface WhatsAppRecordRepository extends JpaRepository<WhatsAppRecord, Long> {
    Optional<WhatsAppRecord> findByWhatsAppMessageId(String messageId);
}
