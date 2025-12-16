package com.grabbill.core.repository;

import com.grabbill.core.entity.EmbeddedLinkClick;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author michaellow
 */
public interface EmbeddedLinkClickRepository
        extends JpaRepository<EmbeddedLinkClick, Long>, EmbeddedLinkClickRepositoryCustom {

    Page<EmbeddedLinkClick> findByEmbeddedLinkIdIs(Long embeddedLinkId, Pageable pageable);

    int countByEmbeddedLinkIdIs(Long embeddedLinkId);

    int countByEmbeddedLinkIdAndEmail(Long embeddedLinkId, String email);

    List<EmbeddedLinkClick> findByEmbeddedLinkIdIs(Long embeddedLinkId);

}
