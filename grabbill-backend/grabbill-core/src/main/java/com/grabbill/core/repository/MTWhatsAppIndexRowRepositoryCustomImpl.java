package com.grabbill.core.repository;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppIndexField;
import com.grabbill.core.entity.MTWhatsAppIndexRow;
import com.grabbill.core.entity.MTWhatsAppType;
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
 * @author seez
 */
@Repository
public class MTWhatsAppIndexRowRepositoryCustomImpl implements MTWhatsAppIndexRowRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<MTWhatsAppIndexRow> searchByFilters(
            final MTWhatsAppType mtWhatsappType,
            final Optional<MTWhatsAppActivity> mtWhatsappActivityOptional,
            final Map<String, String> filters,
            final boolean hasFileOnly,
            final Pageable pageable
            ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<MTWhatsAppIndexRow> query = criteriaBuilder.createQuery(MTWhatsAppIndexRow.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<MTWhatsAppIndexRow> indexRowRoot = query.from(MTWhatsAppIndexRow.class);

        List<MTWhatsAppIndexField> targetIndexFields = mtWhatsappType.getMtWhatsAppIndexFields();
        targetIndexFields.sort(Comparator.comparingInt(MTWhatsAppIndexField::getSeqOrder));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(
                criteriaBuilder.equal(indexRowRoot.get("mtWhatsAppType"), mtWhatsappType)
        );
        mtWhatsappActivityOptional.ifPresent(mtWhatsappActivity -> predicates.add(
                criteriaBuilder.equal(indexRowRoot.get("mtWhatsappActivity"), mtWhatsappActivity)
        ));


        for (MTWhatsAppIndexField indexField : targetIndexFields) {
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

        // only includes index row with WhatsappFile reference
        if (hasFileOnly) {
            predicates.add(
                    criteriaBuilder.isNotNull(indexRowRoot.get("mtWhatsAppFile"))
            );
        }

        query.where(predicates.toArray(new Predicate[0]));
        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), indexRowRoot, criteriaBuilder));
        }
        TypedQuery<MTWhatsAppIndexRow> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        countQuery.where(predicates.toArray(new Predicate[0]));
        countQuery.select(criteriaBuilder.count(countQuery.from(MTWhatsAppIndexRow.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}
