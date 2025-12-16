package com.grabbill.core.repository;

import com.grabbill.core.entity.ContactGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface ContactGroupRepository extends JpaRepository<ContactGroup, Integer> {

    Page<ContactGroup> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Page<ContactGroup> findByAccountIdAndNameContainingIgnoreCase(
            Integer accountId,
            String name,
            Pageable pageable
    );

    Optional<ContactGroup> findByAccountIdAndId(
            Integer accountId,
            Integer id
    );

}
