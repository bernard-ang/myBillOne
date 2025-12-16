package com.grabbill.core.service;

import com.grabbill.core.entity.BaseActivity;
import com.grabbill.core.entity.BaseType;
import com.grabbill.core.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author michaellow
 * @param <T> Type
 * @param <A> Activity
 */
public interface BaseTypeService<T extends BaseType, A extends BaseActivity> {

    Page<T> getAll(User user, Pageable pageable);

    Page<T> getAllByName(User user, String name, Pageable pageable);

    T getByActivity(A activity);

    Optional<T> getById(User user, Long id);

    Optional<T> getByName(User user, String name);

    T save(T type);

    void delete(T type);

    String findNextDuplicateName(String name);

}
