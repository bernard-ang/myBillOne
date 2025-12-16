package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 * @param <T> BaseType
 * @param <A> BaseActivity
 * @param <IDXR> BaseIndexRow
 */
public interface BaseIndexRowService <T extends BaseType, A extends BaseActivity, IDXR extends BaseIndexRow> {

    Page<IDXR> searchByFilters(
            T type,
            Map<String, String> filters,
            boolean hasFileOnly,
            Pageable pageable
    );

    Page<IDXR> searchByFilters(
            T type,
            A activity,
            Map<String, String> filters,
            boolean hasFileOnly,
            Pageable pageable
    );

    Optional<IDXR> getById(Long id);

    IDXR save(IDXR indexRow);

}
