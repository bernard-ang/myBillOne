package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.ContactField;
import com.grabbill.core.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface ContactFieldService {

    String DEFAULT_FIRST_NAME_FIELD = "firstName";
    String DEFAULT_FIRST_NAME_FIELD_LABEL = "First Name";

    String DEFAULT_LAST_NAME_FIELD = "lastName";
    String DEFAULT_LAST_NAME_FIELD_LABEL = "Last Name";

    List<ContactField> getAll(User user);

    List<ContactField> getAll(Account account);

    Optional<ContactField> getById(User user, Integer id);

    ContactField save(ContactField contactField);

    List<ContactField> saveAll(List<ContactField> contactFields);

    void delete(ContactField contactField);

    void deleteAll(List<ContactField> contactFields);

    List<ContactField> createDefaultContactFields(Account account);

}
