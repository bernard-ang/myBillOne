package com.grabbill.core.repository;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailIndexField;
import com.grabbill.core.entity.TransactionalEmailIndexRow;
import com.grabbill.core.entity.TransactionalEmailType;
import com.grabbill.core.model.DataType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.*;

/**
 * @author michaellow
 */
@Repository
public class TransactionalEmailIndexRowRepositoryImpl implements TransactionalEmailIndexRowRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<TransactionalEmailIndexRow> searchByFilters(
            final TransactionalEmailType transactionalEmailType,
            final Optional<TransactionalEmailActivity> transactionalEmailActivityOptional,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
            ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<TransactionalEmailIndexRow> query = criteriaBuilder.createQuery(TransactionalEmailIndexRow.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<TransactionalEmailIndexRow> indexRowRoot = query.from(TransactionalEmailIndexRow.class);

        List<TransactionalEmailIndexField> targetIndexFields = transactionalEmailType.getTransactionalEmailIndexFields();
        targetIndexFields.sort(Comparator.comparingInt(TransactionalEmailIndexField::getSeqOrder));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(
                criteriaBuilder.equal(indexRowRoot.get("transactionalEmailType"), transactionalEmailType)
        );
        transactionalEmailActivityOptional.ifPresent(transactionalEmailActivity -> predicates.add(
                criteriaBuilder.equal(indexRowRoot.get("transactionalEmailActivity"), transactionalEmailActivity)
        ));


        for (TransactionalEmailIndexField indexField : targetIndexFields) {
            String key = "idxf" + indexField.getSeqOrder();
            if (filters.containsKey(key)) {
                if (DataType.TEXT.equals(indexField.getDataType())) {
                    predicates.add(
                            criteriaBuilder.like(
                                    indexRowRoot.get("text" + indexField.getSeqOrder()),
                                    "%" + filters.get(key) + "%"
                            )
                    );

                } else if (DataType.NUMBER.equals(indexField.getDataType())) {
                    predicates.add(
                            criteriaBuilder.equal(
                                    indexRowRoot.get("number" + indexField.getSeqOrder()),
                                    filters.get(key)
                            )
                    );

                } else if (DataType.DATE.equals(indexField.getDataType())) {
                    String value = filters.get(key);
                    if (value.contains(":")) {
                        String[] dates = value.split(":");
                        predicates.add(
                                criteriaBuilder.between(
                                        indexRowRoot.get("date" + indexField.getSeqOrder()),
                                        LocalDate.parse(dates[0]),
                                        LocalDate.parse(dates[1])
                                )
                        );
                    } else {
                        predicates.add(
                                criteriaBuilder.equal(
                                        indexRowRoot.get("date" + indexField.getSeqOrder()),
                                        LocalDate.parse(filters.get(key))
                                )
                        );
                    }
                }
            }
        }

        // only includes index row with TransactionalEmailFile reference
        if (hasFileOnly) {
            predicates.add(
                    criteriaBuilder.isNotNull(indexRowRoot.get("transactionalEmailFile"))
            );
        }

        query.where(predicates.toArray(new Predicate[0]));
        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), indexRowRoot, criteriaBuilder));
        }
        TypedQuery<TransactionalEmailIndexRow> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        countQuery.where(predicates.toArray(new Predicate[0]));
        countQuery.select(criteriaBuilder.count(countQuery.from(TransactionalEmailIndexRow.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}
