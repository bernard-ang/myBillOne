package com.grabbill.core.service;

import com.grabbill.core.entity.ContactGroup;
import com.grabbill.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface ContactGroupService {

    Page<ContactGroup> searchContactGroups(
            User user,
            String name,
            Pageable pageable
    );

    Optional<ContactGroup> getById(User user, Integer id);

    ContactGroup save(ContactGroup contactGroup);

    void delete(ContactGroup contactGroup);

}
