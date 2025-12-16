package com.grabbill.core.repository;

import com.grabbill.core.entity.AdminUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Integer> {

    Optional<AdminUser> findByEmail(String email);

    Page<AdminUser> findByNameIsContainingIgnoreCase(String name, Pageable pageable);

}
