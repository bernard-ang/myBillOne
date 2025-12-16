package com.grabbill.core.repository;

import com.grabbill.core.entity.ContactField;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface ContactFieldRepository extends JpaRepository<ContactField, Integer> {

    Page<ContactField> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    List<ContactField> findByAccountId(Integer accountId);

    Optional<ContactField> findByIdAndAccountId(Integer id, Integer accountId);

}
