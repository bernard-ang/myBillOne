package com.grabbill.core.repository;

import com.grabbill.core.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByNameIs(String name);

}
