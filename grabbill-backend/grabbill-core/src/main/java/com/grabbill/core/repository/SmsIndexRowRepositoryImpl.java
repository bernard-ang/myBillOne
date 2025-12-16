package com.grabbill.core.repository;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.DataType;
import com.grabbill.core.model.SmsFieldType;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author michaellow
 */
@Repository
public class SmsIndexRowRepositoryImpl implements SmsIndexRowRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<SmsIndexRow> searchByFilters(
            final SmsType smsType,
            final List<ContactField> contactFields,
            final Map<String, String> filters,
            final Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SmsIndexRow> query = criteriaBuilder.createQuery(SmsIndexRow.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<SmsIndexRow> indexRowRoot = query.from(SmsIndexRow.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(
                criteriaBuilder.equal(indexRowRoot.get("smsType"), smsType)
        );


        if (SmsFieldType.CONTACT_FIELD.equals(smsType.getSmsFieldType())) {
            // mandatory mobileNo field, data type is fixed to TEXT
            if (filters.containsKey("idxf1")) {
                predicates.add(
                        criteriaBuilder.like(
                                indexRowRoot.get("text1"),
                                "%" + filters.get("idxf1") + "%"
                        )
                );
            }

            // other contact fields
            contactFields.sort(Comparator.comparingInt(ContactField::getSeqOrder));
            for (ContactField contactField : contactFields) {
                int index = contactField.getSeqOrder() + 1;

                String key = "idxf" + index;
                if (filters.containsKey(key)) {
                    if (DataType.TEXT.equals(contactField.getDataType())) {
                        predicates.add(
                                criteriaBuilder.like(
                                        indexRowRoot.get("text" + index),
                                        "%" + filters.get(key) + "%"
                                )
                        );

                    } else if (DataType.NUMBER.equals(contactField.getDataType())) {
                        predicates.add(
                                criteriaBuilder.equal(
                                        indexRowRoot.get("number" + index),
                                        filters.get(key)
                                )
                        );

                    } else if (DataType.DATE.equals(contactField.getDataType())) {
                        String value = filters.get(key);
                        if (value.contains(":")) {
                            String[] dates = value.split(":");
                            predicates.add(
                                    criteriaBuilder.between(
                                            indexRowRoot.get("date" + index),
                                            LocalDate.parse(dates[0]),
                                            LocalDate.parse(dates[1])
                                    )
                            );
                        } else {
                            predicates.add(
                                    criteriaBuilder.equal(
                                            indexRowRoot.get("date" + index),
                                            LocalDate.parse(filters.get(key))
                                    )
                            );
                        }
                    }
                }
            }

        } else if (SmsFieldType.INDEX_FIELD.equals(smsType.getSmsFieldType())) {
            List<SmsIndexField> targetIndexFields = smsType.getSmsIndexFields();
            targetIndexFields.sort(Comparator.comparingInt(SmsIndexField::getSeqOrder));

            for (SmsIndexField indexField : targetIndexFields) {
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



        query.where(predicates.toArray(new Predicate[0]));
        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), indexRowRoot, criteriaBuilder));
        }
        TypedQuery<SmsIndexRow> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        countQuery.where(predicates.toArray(new Predicate[0]));
        countQuery.select(criteriaBuilder.count(countQuery.from(SmsIndexRow.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}
