package com.grabbill.core.service;

import com.grabbill.core.entity.EmbeddedLinkClick;
import com.grabbill.core.repository.EmbeddedLinkClickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author michaellow
 */
public class EmbeddedLinkClickServiceImpl implements EmbeddedLinkClickService {

    @Autowired
    private EmbeddedLinkClickRepository repository;


    @Override
    public Page<EmbeddedLinkClick> getByEmbeddedLinkId(final Long embeddedLinkId, final Pageable pageable) {
        return repository.findByEmbeddedLinkIdIs(embeddedLinkId, pageable);
    }

    @Override
    public List<EmbeddedLinkClick> getByEmbeddedLinkId(Long embeddedLinkId) {
        return repository.findByEmbeddedLinkIdIs(embeddedLinkId);
    }

    @Override
    public int countUniqueClicksByEmbeddedLinkId(final Long embeddedLinkId) {
        return repository.countByDistinctEmailWhereEmbeddedLinkIdIs(embeddedLinkId);
    }

    @Override
    public int countTotalClicksByEmbeddedLinkId(final Long embeddedLinkId) {
        return repository.countByEmbeddedLinkIdIs(embeddedLinkId);
    }

    @Override
    public int countTotalClicksByEmbeddedLinkIdAndEmail(final Long embeddedLinkId, final String email) {
        return repository.countByEmbeddedLinkIdAndEmail(embeddedLinkId, email);
    }

    @Override
    public EmbeddedLinkClick save(final EmbeddedLinkClick target) {
        return repository.save(target);
    }

}
