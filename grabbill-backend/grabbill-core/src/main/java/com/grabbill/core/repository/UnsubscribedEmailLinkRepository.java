package com.grabbill.core.repository;

import com.grabbill.core.entity.UnsubscribedEmailLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface UnsubscribedEmailLinkRepository extends JpaRepository<UnsubscribedEmailLink, Long> {

    Optional<UnsubscribedEmailLink> findByLinkIdIs(String linkId);

}
