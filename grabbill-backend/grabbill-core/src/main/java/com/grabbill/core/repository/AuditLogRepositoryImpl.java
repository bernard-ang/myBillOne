package com.grabbill.core.repository;

import com.grabbill.core.entity.AuditLog;
import com.grabbill.core.model.DomainType;
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
public class AuditLogRepositoryImpl implements AuditLogRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<AuditLog> searchByFilters(
            final Integer accountId,
            final DomainType domainType,
            final String targetQuery,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditLog> query = criteriaBuilder.createQuery(AuditLog.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<AuditLog> root = query.from(AuditLog.class);

        Predicate predicate = criteriaBuilder.equal(root.get("accountId"), accountId);

        if (domainType != null) {
            predicate = criteriaBuilder.and(
                    predicate,
                    criteriaBuilder.equal(root.get("domainType"), domainType)
            );
        }

        if (StringUtils.hasLength(targetQuery)) {
            predicate = criteriaBuilder.and(
                    predicate,
                    criteriaBuilder.or(
                            criteriaBuilder.like(root.get("actionType"), "%" + targetQuery + "%"),
                            criteriaBuilder.like(root.get("description"), "%" + targetQuery + "%")
                    )
            );
        }

        if (startDate != null && endDate != null) {
            predicate = criteriaBuilder.and(
                    predicate,
                    criteriaBuilder.between(root.get("createdDate"), startDate, endDate)
            );

        } else if (startDate != null) {
            predicate = criteriaBuilder.and(
                    predicate,
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startDate)
            );

        } else if (endDate != null) {
            predicate = criteriaBuilder.and(
                    predicate,
                    criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endDate)
            );
        }

        return getAuditlogsInternal(
                criteriaBuilder,
                query,
                countQuery,
                root,
                predicate,
                pageable
        );
    }

    @Override
    public Page<AuditLog> searchByUsername(
            final String username,
            final Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditLog> query = criteriaBuilder.createQuery(AuditLog.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<AuditLog> root = query.from(AuditLog.class);
        Predicate predicate = criteriaBuilder.equal(root.get("createdBy"), username);

        return getAuditlogsInternal(
                criteriaBuilder,
                query,
                countQuery,
                root,
                predicate,
                pageable
        );
    }

    private Page<AuditLog> getAuditlogsInternal(
            final CriteriaBuilder criteriaBuilder,
            final CriteriaQuery<AuditLog> query,
            final CriteriaQuery<Long> countQuery,
            final Root<AuditLog> root,
            final Predicate predicate,
            final Pageable pageable
    ) {
        query.where(predicate);
        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        TypedQuery<AuditLog> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        countQuery.where(predicate);
        countQuery.select(criteriaBuilder.count(countQuery.from(AuditLog.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}