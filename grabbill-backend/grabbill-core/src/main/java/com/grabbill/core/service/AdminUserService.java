package com.grabbill.core.service;

import com.grabbill.core.entity.AdminUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface AdminUserService {

    Optional<AdminUser> getById(Integer id);

    Optional<AdminUser> getByEmail(String email);

    AdminUser save(AdminUser adminUser);

    Page<AdminUser> getAll(Pageable pageable);

    Page<AdminUser> getAllByName(String name, Pageable pageable);

    void delete(AdminUser adminUser);

}
