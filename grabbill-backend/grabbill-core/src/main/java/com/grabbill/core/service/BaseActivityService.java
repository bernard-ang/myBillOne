package com.grabbill.core.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.BaseActivity;
import com.grabbill.core.entity.BaseType;
import com.grabbill.core.model.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 * @param <T> BaseType
 * @param <A> BaseActivity
 */
public interface BaseActivityService<T extends BaseType, A extends BaseActivity> {

    Page<A> getByType(T type, Pageable pageable);

    Page<A> getByTypeAndName(T type, String name, Pageable pageable);

    Page<A> getByTypeAndStatus(T type, ProcessStatus status, Pageable pageable);

    Page<A> getByTypeAndNameAndStatus(T type, String name, ProcessStatus status, Pageable pageable);

    List<A> getByType(T type);

    List<A> getByTypeAndStatusIn(T type, List<ProcessStatus> processStatuses);

    Optional<A> getById(Long id);

    Optional<A> getByNameAndType(String name, T type);

    Optional<A> getByIdAndType(Long id, T type);

    List<A> getAllByStatusInAndNotPurged(List<ProcessStatus> processStatuses);

    A save(A activity);

    A saveAndFlush(A activity);

    void delete(A activity);

    int countAllSubmittedActivities();

    List<A> getAllProcessedActivities();

    A markAsProcessing(Long id);

    List<A> getAllByAccountBetween(Account account, OffsetDateTime start, OffsetDateTime end);

    default A updateCount(A activity) {
        // no op
        return activity;
    }
}
