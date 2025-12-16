package com.grabbill.core.repository;

import com.grabbill.core.entity.Job;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.job.JobStatus;
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
public class JobRepositoryImpl implements JobRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<Job> searchJobs(
            final String accountName,
            final DomainType domainType,
            final String activity,
            final JobStatus status,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Job> query = criteriaBuilder.createQuery(Job.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<Job> root = query.from(Job.class);

        Predicate predicate = null;
        if (accountName != null) {
            root.fetch("account", JoinType.LEFT);
            predicate = criteriaBuilder.like(root.get("account").get("companyName"), "%" + accountName + "%");
        }

        if (domainType != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.equal(root.get("domainType"), domainType);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.equal(root.get("domainType"), domainType)
                );
            }
        }

        if (activity != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.like(root.get("activityName"), "%" + activity + "%");

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(root.get("activityName"), "%" + activity + "%")
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
                predicate = criteriaBuilder.greaterThanOrEqualTo(root.get("createdTimestamp"), startDate);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("createdTimestamp"), startDate)
                );
            }
        }

        if (endDate != null) {
            if (predicate == null) {
                predicate = criteriaBuilder.lessThanOrEqualTo(root.get("createdTimestamp"), endDate);

            } else {
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("createdTimestamp"), endDate)
                );
            }
        }

        if (predicate != null) {
            query.where(predicate);
        }

        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        TypedQuery<Job> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        if (predicate != null) {
            countQuery.where(predicate);
        }
        countQuery.select(criteriaBuilder.count(countQuery.from(Job.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}
