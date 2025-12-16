package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.WhatsAppRecord;
import com.grabbill.core.repository.WhatsAppRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author seez
 */
public class WhatsAppRecordServiceImpl implements WhatsAppRecordService {

    @Autowired
    private WhatsAppRecordRepository repository;

    @Override
    public Optional<WhatsAppRecord> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public WhatsAppRecord save(final WhatsAppRecord whatsAppRecord) {
        return repository.save(whatsAppRecord);
    }

    @Override
    public Optional<WhatsAppRecord> findByWhatsAppMessageId(String messageId) {
        return repository.findByWhatsAppMessageId(messageId);
    }
}
