package com.grabbill.core.service;

import com.grabbill.core.entity.Role;
import com.grabbill.core.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author seez
 */
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;


    @Override
    public Optional<Role> getByName(String name) {
        return roleRepository.findByNameIs(name);
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> getById(Integer id) {
        return roleRepository.findById(id);
    }

    @Override
    public Role save(final Role role) {
        return roleRepository.save(role);
    }

    @Override
    public void delete(final Role role) {
        roleRepository.delete(role);
    }

}
