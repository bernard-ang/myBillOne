package com.grabbill.core.repository;

import com.grabbill.core.entity.MTWhatsAppRecord;
import com.grabbill.core.entity.MTWhatsAppActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTWhatsAppRecordRepository extends JpaRepository<MTWhatsAppRecord, Long> {

    Optional<MTWhatsAppRecord> findByWhatsAppMessageId(String messageId);

    long countByWhatsAppStatusSentTrueAndMtWhatsAppActivity(MTWhatsAppActivity mtWhatsAppActivity);
    long countByWhatsAppStatusSkipTrueAndMtWhatsAppActivity(MTWhatsAppActivity mtWhatsAppActivity);
    long countByWhatsAppStatusDeliveredTrueAndMtWhatsAppActivity(MTWhatsAppActivity mtWhatsAppActivity);
    long countByWhatsAppStatusReadTrueAndMtWhatsAppActivity(MTWhatsAppActivity mtWhatsAppActivity);
    long countByWhatsAppStatusAcknowledgeTrueAndMtWhatsAppActivity(MTWhatsAppActivity mtWhatsAppActivity);
    long countByWhatsAppStatusFailedTrueAndMtWhatsAppActivity(MTWhatsAppActivity mtWhatsAppActivity);

}
