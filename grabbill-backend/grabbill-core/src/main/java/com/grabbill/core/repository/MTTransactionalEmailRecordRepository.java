package com.grabbill.core.repository;

import com.grabbill.core.entity.MTTransactionalEmailRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTTransactionalEmailRecordRepository extends JpaRepository<MTTransactionalEmailRecord, Long> {

    Optional<MTTransactionalEmailRecord> findByWhatsappMessageId(String messageId);

}
