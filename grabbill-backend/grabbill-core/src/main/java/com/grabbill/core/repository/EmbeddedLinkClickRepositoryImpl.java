package com.grabbill.core.repository;

import com.grabbill.core.entity.EmbeddedLinkClick;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 * @author michaellow
 */
@Repository
public class EmbeddedLinkClickRepositoryImpl implements EmbeddedLinkClickRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public int countByDistinctEmailWhereEmbeddedLinkIdIs(final Long embeddedLinkId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<EmbeddedLinkClick> root = countQuery.from(EmbeddedLinkClick.class);
        countQuery.select(criteriaBuilder.countDistinct(root.get("email")));
        countQuery.where(criteriaBuilder.equal(root.get("embeddedLink").get("id"), embeddedLinkId));

        return entityManager.createQuery(countQuery).getSingleResult().intValue();
    }

}
