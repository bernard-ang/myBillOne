package com.grabbill.core.service;

import com.grabbill.core.entity.MTWhatsAppRecord;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTWhatsAppRecordService extends BaseRecordService<MTWhatsAppRecord> {

    Optional<MTWhatsAppRecord> findById(Long id);

    Optional<MTWhatsAppRecord> findByWhatsAppMessageId(String messageId);

}
