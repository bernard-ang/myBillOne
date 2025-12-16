package com.grabbill.core.repository;

import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.StripeEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author michaellow
 */
public interface StripeEventRepository extends JpaRepository<StripeEvent, Long>, StripeEventRepositoryCustom {

    List<StripeEvent> findByTypeAndRefId(StripeEventType stripeEventType, String refId);

}
