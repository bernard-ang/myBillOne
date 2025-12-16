package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DomainType;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface EmbeddedLinkService {

    Optional<EmbeddedLink> getByIdAndDomainType(Long id, DomainType domainType);

    Optional<EmbeddedLink> getByDomainTypeAndTypeIdAndActivityIdAndId(DomainType domainType, Long typeId, Long activityId, Long linkId);

    List<EmbeddedLink> getByDomainTypeAndActivityIdIs(DomainType domainType, Long activityId);

    EmbeddedLink save(EmbeddedLink target);

}
