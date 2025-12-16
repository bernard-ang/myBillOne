package com.grabbill.core.repository;

import com.grabbill.core.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface ContactRepository extends JpaRepository<Contact, Integer>, ContactRepositoryCustom {

    List<Contact> findByAccountId(Integer accountId);

    Optional<Contact> findByAccountIdAndId(Integer accountId, Integer id);

    List<Contact> findByAccountIdAndEmail(Integer accountId, String email);

    boolean existsByAccountIdAndEmail(Integer accountId, String email);

    void deleteByAccountIdAndIdIn(Integer accountId, List<Integer> ids);

}
