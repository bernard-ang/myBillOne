package com.grabbill.core.repository;

/**
 * @author michaellow
 */
public interface EmbeddedLinkClickRepositoryCustom {

    int countByDistinctEmailWhereEmbeddedLinkIdIs(Long embeddedLinkId);

}
