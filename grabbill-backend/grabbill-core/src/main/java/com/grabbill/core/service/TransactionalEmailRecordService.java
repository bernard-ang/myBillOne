package com.grabbill.core.service;

import com.grabbill.core.entity.TransactionalEmailRecord;
import com.grabbill.core.repository.TransactionalEmailRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface TransactionalEmailRecordService extends BaseRecordService<TransactionalEmailRecord> {

    Optional<TransactionalEmailRecord> findById(Long id);

    Optional<TransactionalEmailRecord> findByWhatsappMessageId(String messageId);

}
