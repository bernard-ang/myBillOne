package com.grabbill.core.service;

import com.grabbill.core.entity.EmbeddedLink;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.repository.EmbeddedLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class EmbeddedLinkServiceImpl implements EmbeddedLinkService {

    @Autowired
    private EmbeddedLinkRepository repository;


    @Override
    public Optional<EmbeddedLink> getByIdAndDomainType(final Long id, final DomainType domainType) {
        return repository.findByIdAndDomainType(id, domainType);
    }

    @Override
    public Optional<EmbeddedLink> getByDomainTypeAndTypeIdAndActivityIdAndId(
            final DomainType domainType,
            final Long typeId,
            final Long activityId,
            final Long linkId
    ) {
        return repository.findByDomainTypeAndTypeIdAndActivityIdAndId(domainType, typeId, activityId, linkId);
    }

    @Override
    public List<EmbeddedLink> getByDomainTypeAndActivityIdIs(
            final DomainType domainType,
            final Long activityId
    ) {
        return repository.findByDomainTypeAndActivityId(domainType, activityId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public EmbeddedLink save(final EmbeddedLink target) {
        return repository.saveAndFlush(target);
    }

}
