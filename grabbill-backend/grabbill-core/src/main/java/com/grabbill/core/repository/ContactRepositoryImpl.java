package com.grabbill.core.repository;

import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.ContactField;
import com.grabbill.core.entity.ContactGroup;
import com.grabbill.core.model.DataType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author michaellow
 */
@Repository
public class ContactRepositoryImpl implements ContactRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Page<Contact> searchByFilters(
            final Integer accountId,
            final Map<String, String> filters,
            final List<ContactField> contactFields,
            final Pageable pageable
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Contact> query = criteriaBuilder.createQuery(Contact.class);
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        Root<Contact> contactRoot = query.from(Contact.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(
                criteriaBuilder.equal(contactRoot.get("account").get("id"), accountId)
        );

        if (filters.containsKey("group")) {
            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<Contact> subqueryContact = subquery.from(Contact.class);
            Join<ContactGroup, Contact> subqueryGroup = subqueryContact.join("contactGroups");

            subquery.select(subqueryContact.get("id")).where(
                    criteriaBuilder.like(subqueryGroup.get("name"), "%" + filters.get("group") + "%"));

            predicates.add(criteriaBuilder.in(contactRoot.get("id")).value(subquery));
        }

        if (filters.containsKey("email")) {
            predicates.add(
                    criteriaBuilder.like(
                            contactRoot.get("email"), "%" + filters.get("email") + "%"
                    )
            );
        }

        if (filters.containsKey("mobileNo")) {
            predicates.add(
                    criteriaBuilder.like(
                            contactRoot.get("mobileNo"), "%" + filters.get("mobileNo") + "%"
                    )
            );
        }

        contactFields.sort(Comparator.comparingInt(ContactField::getSeqOrder));
        for (ContactField contactField : contactFields) {
            String key = "ctf" + contactField.getSeqOrder();
            if (filters.containsKey(key)) {
                if (DataType.TEXT.equals(contactField.getDataType())) {
                    predicates.add(
                            criteriaBuilder.like(
                                    contactRoot.get("text" + contactField.getSeqOrder()),
                                    "%" + filters.get(key) + "%"
                            )
                    );

                } else if (DataType.NUMBER.equals(contactField.getDataType())) {
                    predicates.add(
                            criteriaBuilder.equal(
                                    contactRoot.get("number" + contactField.getSeqOrder()),
                                    filters.get(key)
                            )
                    );

                } else if (DataType.DATE.equals(contactField.getDataType())) {
                    predicates.add(
                            criteriaBuilder.equal(
                                    contactRoot.get("date" + contactField.getSeqOrder()),
                                    LocalDate.parse(filters.get(key))
                            )
                    );
                }
            }
        }


        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        if (!pageable.getSort().isEmpty()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), contactRoot, criteriaBuilder));
        }
        TypedQuery<Contact> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        typedQuery.setMaxResults(pageable.getPageSize());

        countQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        countQuery.select(criteriaBuilder.count(countQuery.from(Contact.class)));
        long totalRows = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(typedQuery.getResultList(), pageable, totalRows);
    }

}
