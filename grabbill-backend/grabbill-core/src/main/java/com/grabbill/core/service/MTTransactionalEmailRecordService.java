package com.grabbill.core.service;

import com.grabbill.core.entity.MTTransactionalEmailRecord;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTTransactionalEmailRecordService extends BaseRecordService<MTTransactionalEmailRecord> {

    Optional<MTTransactionalEmailRecord> findById(Long id);

    Optional<MTTransactionalEmailRecord> findByWhatsappMessageId(String messageId);

}
