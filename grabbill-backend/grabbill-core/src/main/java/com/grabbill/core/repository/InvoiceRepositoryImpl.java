package com.grabbill.core.repository;

import com.grabbill.core.entity.Invoice;
import com.grabbill.core.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author michaellow
 */
public class InvoiceRepositoryImpl implements InvoiceRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<Invoice> searchInvoices(
            final String accountName,
            final String invoiceNo,
            final String planName,
            final InvoiceStatus status,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Invoice> query = criteriaBuilder.createQuery(Invoice.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<Invoice> root = query.from(Invoice.class);

        Predicate predicate = null;
        if (accountName != null) {
            root.fetch("account", JoinType.LEFT);
            predicate = criteriaBuilder.like(root.get("account").get("companyName"), "%" + accountName + "%");
        }

        if (invoiceNo != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.like(root.get("invoiceNo"), "%" + invoiceNo + "%");

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("invoiceNo"), "%" + invoiceNo + "%")
                );
            }
        }

        if (planName != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.like(root.get("planName"), "%" + planName + "%");

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("planName"), "%" + planName + "%")
                );
            }
        }

        if (status != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.equal(root.get("status"), status);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("status"), status)
                );
            }
        }

        if (startDate != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("cycleStartDate"), startDate);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("cycleStartDate"), startDate)
                );
            }
        }

        if (endDate != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.lessThanOrEqualTo(root.get("cycleStartDate"), endDate);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("cycleStartDate"), endDate)
                );
            }
        }

        if (predicate != null) {
            query.where(predicate);
        }

        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        TypedQuery<Invoice> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        if (predicate != null) {
            countQuery.where(predicate);
        }
        countQuery.select(criteriaBuilder.count(countQuery.from(Invoice.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

    @Override
    public List<Invoice> findInvoicesBetweenAndAccountIdIn(
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Set<Integer> accountIds
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Invoice> query = criteriaBuilder.createQuery(Invoice.class);
        Root<Invoice> root = query.from(Invoice.class);

        Predicate predicate = null;
        if (startDate != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("cycleStartDate"), startDate);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("cycleStartDate"), startDate)
                );
            }
        }

        if (endDate != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.lessThanOrEqualTo(root.get("cycleStartDate"), endDate);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("cycleStartDate"), endDate)
                );
            }
        }

        if (accountIds != null && !accountIds.isEmpty()) {
            Expression<Integer> expression = root.get("account");

            if (predicate == null) {
                predicate = expression.in(accountIds);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        expression.in(accountIds)
                );
            }
        }

        if (predicate != null) {
            query.where(predicate);
            query.orderBy(QueryUtils.toOrders(Sort.by(Sort.Order.asc("cycleStartDate")), root, criteriaBuilder));
        }

        TypedQuery<Invoice> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }
}
