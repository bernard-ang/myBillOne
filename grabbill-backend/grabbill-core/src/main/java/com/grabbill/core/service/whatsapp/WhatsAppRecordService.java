package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.WhatsAppRecord;
import com.grabbill.core.service.BaseRecordService;

import java.util.Optional;

/**
 * @author seez
 */
public interface WhatsAppRecordService extends BaseRecordService<WhatsAppRecord> {
    Optional<WhatsAppRecord> findById(Long id);

    Optional<WhatsAppRecord> findByWhatsAppMessageId(String messageId);
}
