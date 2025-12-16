package com.grabbill.core.service;

import com.grabbill.core.entity.AdminUser;
import com.grabbill.core.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author michaellow
 */
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private AdminUserRepository repository;


    @Override
    public Optional<AdminUser> getById(final Integer id) {
        return repository.findById(id);
    }

    @Override
    public Optional<AdminUser> getByEmail(final String email) {
        return repository.findByEmail(email);
    }

    @Override
    public AdminUser save(final AdminUser adminUser) {
        return repository.save(adminUser);
    }

    @Override
    public Page<AdminUser> getAll(final Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<AdminUser> getAllByName(final String name, final Pageable pageable) {
        return repository.findByNameIsContainingIgnoreCase(name, pageable);
    }

    @Override
    public void delete(AdminUser adminUser) {
        repository.delete(adminUser);
    }

}
