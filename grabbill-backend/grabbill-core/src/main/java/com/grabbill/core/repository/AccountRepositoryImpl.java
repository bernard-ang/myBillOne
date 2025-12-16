package com.grabbill.core.repository;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountSubscription;
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
 * @author seez
 */
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Account> searchAccount(String companyName, String planName, OffsetDateTime startCreatedDate, OffsetDateTime endCreatedDate, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Account> query = criteriaBuilder.createQuery(Account.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<Account> root = query.from(Account.class);
        query.where(getSearchPredicate(companyName, planName, startCreatedDate, endCreatedDate, criteriaBuilder, root));
        query.distinct(true);

        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, criteriaBuilder));
        }
        TypedQuery<Account> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        Root<Account> countRoot = countQuery.from(Account.class);
        countQuery.where(getSearchPredicate(companyName, planName, startCreatedDate, endCreatedDate, criteriaBuilder, countRoot));
        countQuery.distinct(true);
        countQuery.select(criteriaBuilder.countDistinct(countRoot));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

    private static Predicate getSearchPredicate(final String companyName, final String planName, final OffsetDateTime startCreatedDate, final OffsetDateTime endCreatedDate, final CriteriaBuilder criteriaBuilder, final Root<Account> root) {
        Join<Account, AccountSubscription> join = root.join("subscriptions", JoinType.LEFT);

        Predicate predicate = criteriaBuilder.or(
                criteriaBuilder.equal(join.get("account"), root.get("id")),
                criteriaBuilder.isNull(join.get("account"))
        );

        if (companyName != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("companyName"), "%" + companyName + "%"));
        }

        if (startCreatedDate != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("createdDate"), startCreatedDate));
        }

        if (endCreatedDate != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThanOrEqualTo(root.get("createdDate"), endCreatedDate));
        }

        if (planName != null) {
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(join.get("planName"), "%" + planName + "%"));
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.and(predicate, criteriaBuilder.isNull(join.get("endDate"))));
        }
        return predicate;
    }
}
