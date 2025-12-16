package com.grabbill.core.service;

import com.grabbill.core.entity.TransactionalEmailRecord;
import com.grabbill.core.repository.TransactionalEmailRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author michaellow
 */
public class TransactionalEmailRecordServiceImpl implements TransactionalEmailRecordService {

    @Autowired
    private TransactionalEmailRecordRepository repository;

    @Override
    public Optional<TransactionalEmailRecord> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public TransactionalEmailRecord save(final TransactionalEmailRecord transactionalEmailRecord) {
        return repository.save(transactionalEmailRecord);
    }

    @Override
    public Optional<TransactionalEmailRecord> findByWhatsappMessageId(String messageId) {
        return repository.findByWhatsappMessageId(messageId);
    }
}
