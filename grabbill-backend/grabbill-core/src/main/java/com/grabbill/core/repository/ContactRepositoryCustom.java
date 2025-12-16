package com.grabbill.core.repository;

import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.ContactField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author michaellow
 */
public interface ContactRepositoryCustom {

    Page<Contact> searchByFilters(
            Integer accountId,
            Map<String, String> filters,
            List<ContactField> contactFields,
            Pageable pageable
    );

}
