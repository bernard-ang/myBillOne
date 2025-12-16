package com.grabbill.core.service;

import com.grabbill.core.entity.*;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 * @param <T> BaseType
 * @param <A> BaseActivity
 * @param <F> BaseFile
 */
public interface BaseFileService<T extends BaseType, A extends BaseActivity, F extends BaseFile> {

    List<F> getByActivity(A activity);

    Optional<F> getByNameAndActivity(String filename, A activity);

    F save(F file);

    void delete(F file);

}
