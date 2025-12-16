package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.OffsetDateTime;

/**
 * @author seez
 */
@Repository
public class WhatsAppEventRepositoryImpl implements WhatsAppEventRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<WhatsAppEvent> searchWhatsAppEvents(Integer accountId, String messageType, String mobileNo, OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<WhatsAppEvent> query = criteriaBuilder.createQuery(WhatsAppEvent.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<WhatsAppEvent> root = query.from(WhatsAppEvent.class);

        root.fetch("account", JoinType.LEFT);
        Predicate predicate = criteriaBuilder.equal(root.get("account").get("id"), accountId);
        predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("type"), "user-initiated"));

        if (messageType != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.equal(root.get("messageType"), messageType);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("messageType"), messageType)
                );
            }
        }

        if (mobileNo != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.like(root.get("mobileNo"), "%" + mobileNo + "%");

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("mobileNo"), "%" + mobileNo + "%")
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
        TypedQuery<WhatsAppEvent> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        if (predicate != null) {
            countQuery.where(predicate);
        }
        countQuery.select(criteriaBuilder.count(countQuery.from(WhatsAppEvent.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }
}
