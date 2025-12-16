package com.grabbill.core.repository;

import com.grabbill.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndAccountId(Integer id, Integer accountId);

    List<User> findByAccountId(Integer accountId);

    Page<User> findByAccountIdAndRole_NameNot(
            Integer accountId,
            String roleName,
            Pageable pageable
    );

    Page<User> findByNameIsContainingIgnoreCaseAndAccountIdAndRole_NameNot(
            String name,
            Integer accountId,
            String roleName,
            Pageable pageable
    );

}
