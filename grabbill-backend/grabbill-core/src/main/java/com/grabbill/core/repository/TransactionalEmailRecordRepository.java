package com.grabbill.core.repository;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.TransactionalEmailRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface TransactionalEmailRecordRepository extends JpaRepository<TransactionalEmailRecord, Long> {
    Optional<TransactionalEmailRecord> findByWhatsappMessageId(String messageId);
}
