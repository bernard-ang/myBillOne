package com.grabbill.core.service;

import com.grabbill.core.entity.EmbeddedLinkClick;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author michaellow
 */
public interface EmbeddedLinkClickService {

    Page<EmbeddedLinkClick> getByEmbeddedLinkId(Long embeddedLinkId, Pageable pageable);

    List<EmbeddedLinkClick> getByEmbeddedLinkId(Long embeddedLinkId);

    int countUniqueClicksByEmbeddedLinkId(Long embeddedLinkId);

    int countTotalClicksByEmbeddedLinkId(Long embeddedLinkId);

    int countTotalClicksByEmbeddedLinkIdAndEmail(Long embeddedLinkId, String email);

    EmbeddedLinkClick save(EmbeddedLinkClick target);

}
