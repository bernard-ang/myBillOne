package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Role;
import com.grabbill.core.entity.User;
import com.grabbill.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public User getAccountOwner(final Account account) {
        User accountOwner = null;
        for (User user : userRepository.findByAccountId(account.getId())) {
            if (user.getRole().isOwner()) {
                return user;
            }
        }

        return accountOwner;
    }

    @Override
    public Optional<User> getByIdAndAccountId(Integer id, Integer accountId) {
        return userRepository.findByIdAndAccountId(id, accountId);
    }

    @Override
    public List<User> getByAccountId(Integer accountId) {
        return userRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<User> getByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(final User user) {
        return userRepository.save(user);
    }

    @Override
    public Page<User> getAll(User user, Pageable pageable) {
        return userRepository.findByAccountIdAndRole_NameNot(user.getAccount().getId(), Role.OWNER_NAME, pageable);
    }

    @Override
    public Page<User> getAllByName(User user, String name, Pageable pageable) {
        return userRepository.findByNameIsContainingIgnoreCaseAndAccountIdAndRole_NameNot(name, user.getAccount().getId(), Role.OWNER_NAME, pageable);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

}
