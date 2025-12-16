package com.grabbill.core.repository;

import com.grabbill.core.entity.EmbeddedLink;
import com.grabbill.core.model.DomainType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface EmbeddedLinkRepository extends JpaRepository<EmbeddedLink, Long> {

    Optional<EmbeddedLink> findByIdAndDomainType(Long id, DomainType domainType);

    Optional<EmbeddedLink> findByDomainTypeAndTypeIdAndActivityIdAndId(DomainType domainType, Long typeId, Long activityId, Long linkId);

    List<EmbeddedLink> findByDomainTypeAndActivityId(DomainType domainType, Long activityId);

}
