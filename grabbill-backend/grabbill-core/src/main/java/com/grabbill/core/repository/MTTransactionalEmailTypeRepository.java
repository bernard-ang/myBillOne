package com.grabbill.core.repository;

import com.grabbill.core.entity.MTTransactionalEmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface MTTransactionalEmailTypeRepository extends JpaRepository<MTTransactionalEmailType, Long> {

    Optional<MTTransactionalEmailType> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Optional<MTTransactionalEmailType> findByNameAndAccountId(
            String name,
            Integer accountId
    );

    Page<MTTransactionalEmailType> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Page<MTTransactionalEmailType> findByAccountIdAndCodeIn(
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    Page<MTTransactionalEmailType> findByNameIsContainingIgnoreCaseAndAccountId(
            String name,
            Integer accountId,
            Pageable pageable
    );

    Page<MTTransactionalEmailType> findByNameIsContainingIgnoreCaseAndAccountIdAndCodeIn(
            String name,
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    List<MTTransactionalEmailType> findByNameStartsWithOrderByNameAsc(
            String name
    );

}
