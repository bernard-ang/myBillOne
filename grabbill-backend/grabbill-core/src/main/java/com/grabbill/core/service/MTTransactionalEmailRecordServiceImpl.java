package com.grabbill.core.service;

import com.grabbill.core.entity.MTTransactionalEmailRecord;
import com.grabbill.core.repository.MTTransactionalEmailRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author michaellow
 */
public class MTTransactionalEmailRecordServiceImpl implements MTTransactionalEmailRecordService {

    @Autowired
    private MTTransactionalEmailRecordRepository repository;

    @Override
    public Optional<MTTransactionalEmailRecord> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public MTTransactionalEmailRecord save(final MTTransactionalEmailRecord mtTransactionalEmailRecord) {
        return repository.save(mtTransactionalEmailRecord);
    }

    @Override
    public Optional<MTTransactionalEmailRecord> findByWhatsappMessageId(String messageId) {
        return repository.findByWhatsappMessageId(messageId);
    }
}
