package com.grabbill.core.service;

import com.grabbill.core.entity.Contact;
import com.grabbill.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface ContactService {

    Page<Contact> searchByFilters(
            User user,
            Map<String, String> filters,
            Pageable pageable
    );

    Optional<Contact> getById(
            User user,
            Integer id
    );

    boolean existsByEmail(
            User user,
            String email
    );

    List<Contact> getByEmail(
            User user,
            String email
    );

    List<Contact> getAll(User user);

    Contact save(Contact contact);

    List<Contact> saveAll(List<Contact> contacts);

    void delete(Contact contact);

    void deleteAllByIds(User user, List<Integer> ids);

    long count();
}
