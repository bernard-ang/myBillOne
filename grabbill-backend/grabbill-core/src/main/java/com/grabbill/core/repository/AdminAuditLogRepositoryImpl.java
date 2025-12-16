package com.grabbill.core.repository;

import com.grabbill.core.entity.AdminAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.OffsetDateTime;

/**
 * @author michaellow
 */
@Repository
public class AdminAuditLogRepositoryImpl implements AdminAuditLogRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<AdminAuditLog> searchByFilters(
            final String targetQuery,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AdminAuditLog> query = criteriaBuilder.createQuery(AdminAuditLog.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<AdminAuditLog> root = query.from(AdminAuditLog.class);

        Predicate predicate = null;
        if (StringUtils.hasLength(targetQuery)) {
            predicate = criteriaBuilder.or(
                    criteriaBuilder.like(root.get("actionType"), "%" + targetQuery + "%"),
                    criteriaBuilder.like(root.get("description"), "%" + targetQuery + "%")
            );
        }

        if (startDate != null && endDate != null) {
            predicate = (predicate == null) ?
                    criteriaBuilder.between(root.get("createdDate"), startDate, endDate)
                    : criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.between(root.get("createdDate"), startDate, endDate)
            );

        } else if (startDate != null) {
            predicate = (predicate == null) ?
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate)
                    : criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate)
            );

        } else if (endDate != null) {
            predicate = (predicate == null) ? criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate)
                    : criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate)
            );
        }

        if (predicate != null) {
            query.where(predicate);
            countQuery.where(predicate);
        }

        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        TypedQuery<AdminAuditLog> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        countQuery.select(criteriaBuilder.count(countQuery.from(AdminAuditLog.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}