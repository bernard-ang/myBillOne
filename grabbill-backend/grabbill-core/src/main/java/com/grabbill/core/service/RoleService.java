package com.grabbill.core.service;

import com.grabbill.core.entity.Role;

import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public interface RoleService {

    Optional<Role> getByName(String name);

    List<Role> getAll();

    Optional<Role> getById(Integer id);

    Role save(Role role);

    void delete(Role role);

}
