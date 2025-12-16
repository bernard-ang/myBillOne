package com.grabbill.core.repository;

import com.grabbill.core.entity.MTWhatsAppType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface MTWhatsAppTypeRepository extends JpaRepository<MTWhatsAppType, Long> {

    Optional<MTWhatsAppType> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Optional<MTWhatsAppType> findByNameAndAccountId(
            String name,
            Integer accountId
    );

    Page<MTWhatsAppType> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Page<MTWhatsAppType> findByAccountIdAndCodeIn(
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    Page<MTWhatsAppType> findByNameIsContainingIgnoreCaseAndAccountId(
            String name,
            Integer accountId,
            Pageable pageable
    );

    Page<MTWhatsAppType> findByNameIsContainingIgnoreCaseAndAccountIdAndCodeIn(
            String name,
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    List<MTWhatsAppType> findByNameStartsWithOrderByNameAsc(
            String name
    );

}
