package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface UserService {

    User getAccountOwner(Account account);

    Optional<User> getByIdAndAccountId(Integer id, Integer accountId);

    List<User> getByAccountId(Integer accountId);

    Optional<User> getByEmail(String email);

    User save(User user);

    Page<User> getAll(User user, Pageable pageable);

    Page<User> getAllByName(User user, String name, Pageable pageable);

    void delete(User user);

}
