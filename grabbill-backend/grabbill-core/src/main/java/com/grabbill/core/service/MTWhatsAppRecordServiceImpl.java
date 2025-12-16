package com.grabbill.core.service;

import com.grabbill.core.entity.MTWhatsAppRecord;
import com.grabbill.core.repository.MTWhatsAppRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author michaellwo
 */
public class MTWhatsAppRecordServiceImpl implements MTWhatsAppRecordService {

    @Autowired
    private MTWhatsAppRecordRepository repository;

    @Override
    public Optional<MTWhatsAppRecord> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public MTWhatsAppRecord save(final MTWhatsAppRecord mtWhatsAppRecord) {
        return repository.save(mtWhatsAppRecord);
    }

    @Override
    public Optional<MTWhatsAppRecord> findByWhatsAppMessageId(String messageId) {
        return repository.findByWhatsAppMessageId(messageId);
    }

}
