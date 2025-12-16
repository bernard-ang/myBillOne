package com.grabbill.core.repository;

import com.grabbill.core.entity.*;
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
public class EmailCampaignIndexRowRepositoryImpl implements EmailCampaignIndexRowRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<EmailCampaignIndexRow> searchByFilters(
            final EmailCampaignType emailCampaignType,
            final List<ContactField> contactFields,
            final Map<String, String> filters,
            final Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<EmailCampaignIndexRow> query = criteriaBuilder.createQuery(EmailCampaignIndexRow.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<EmailCampaignIndexRow> indexRowRoot = query.from(EmailCampaignIndexRow.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(
                criteriaBuilder.equal(indexRowRoot.get("emailCampaignType"), emailCampaignType)
        );

        // mandatory email field, data type is fixed to TEXT
        if (filters.containsKey("idxf1")) {
            predicates.add(
                    criteriaBuilder.like(
                            indexRowRoot.get("text1"),
                            "%" + filters.get("idxf1") + "%"
                    )
            );
        }

        // mandatory mobileNo field, data type is fixed to TEXT
        if (filters.containsKey("idxf2")) {
            predicates.add(
                    criteriaBuilder.like(
                            indexRowRoot.get("text2"),
                            "%" + filters.get("idxf2") + "%"
                    )
            );
        }

        // other contact fields
        contactFields.sort(Comparator.comparingInt(ContactField::getSeqOrder));
        for (ContactField contactField : contactFields) {
            int index = contactField.getSeqOrder() + 2;

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

        query.where(predicates.toArray(new Predicate[0]));
        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), indexRowRoot, criteriaBuilder));
        }
        TypedQuery<EmailCampaignIndexRow> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        countQuery.where(predicates.toArray(new Predicate[0]));
        countQuery.select(criteriaBuilder.count(countQuery.from(EmailCampaignIndexRow.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}
