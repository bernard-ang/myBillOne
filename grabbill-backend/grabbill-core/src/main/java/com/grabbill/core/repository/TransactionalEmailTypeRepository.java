package com.grabbill.core.repository;

import com.grabbill.core.entity.TransactionalEmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface TransactionalEmailTypeRepository extends JpaRepository<TransactionalEmailType, Long> {

    Optional<TransactionalEmailType> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Optional<TransactionalEmailType> findByNameAndAccountId(
            String name,
            Integer accountId
    );

    Page<TransactionalEmailType> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Page<TransactionalEmailType> findByAccountIdAndCodeIn(
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    Page<TransactionalEmailType> findByNameIsContainingIgnoreCaseAndAccountId(
            String name,
            Integer accountId,
            Pageable pageable
    );

    Page<TransactionalEmailType> findByNameIsContainingIgnoreCaseAndAccountIdAndCodeIn(
            String name,
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    List<TransactionalEmailType> findByNameStartsWithOrderByNameAsc(
            String name
    );

}
