package com.grabbill.core.repository;

import com.grabbill.core.entity.StripeEvent;
import com.grabbill.core.model.StripeEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
public class StripeEventRepositoryImpl implements StripeEventRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<StripeEvent> searchStripeEvents(
            final String accountName,
            final StripeEventType type,
            final String refId,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StripeEvent> query = criteriaBuilder.createQuery(StripeEvent.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<StripeEvent> root = query.from(StripeEvent.class);

        Predicate predicate = null;
        if (accountName != null) {
            root.fetch("account", JoinType.LEFT);
            predicate = criteriaBuilder.like(root.get("account").get("companyName"), "%" + accountName + "%");
        }

        if (type != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.equal(root.get("type"), type);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("type"), type)
                );
            }
        }

        if (refId != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.like(root.get("ref_id"), "%" + refId + "%");

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("ref_id"), "%" + refId + "%")
                );
            }
        }

        if (startDate != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate)
                );
            }
        }

        if (endDate != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate)
                );
            }
        }

        if (predicate != null) {
            query.where(predicate);
        }

        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        TypedQuery<StripeEvent> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        if (predicate != null) {
            countQuery.where(predicate);
        }
        countQuery.select(criteriaBuilder.count(countQuery.from(StripeEvent.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}
