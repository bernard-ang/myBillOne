package com.grabbill.core.repository;

import com.grabbill.core.entity.SmsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface SmsTypeRepository extends JpaRepository<SmsType, Long> {

    Optional<SmsType> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Optional<SmsType> findByNameAndAccountId(
            String name,
            Integer accountId
    );

    Page<SmsType> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Page<SmsType> findByAccountIdAndCodeIn(
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    Page<SmsType> findByNameIsContainingIgnoreCaseAndAccountId(
            String name,
            Integer accountId,
            Pageable pageable
    );

    Page<SmsType> findByNameIsContainingIgnoreCaseAndAccountIdAndCodeIn(
            String name,
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    List<SmsType> findByNameStartsWithOrderByNameAsc(
            String name
    );

    List<SmsType> findByAccountId(
            Integer accountId
    );

}
