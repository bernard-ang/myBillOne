package com.grabbill.core.repository;

import com.grabbill.core.entity.WhatsAppType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author seez
 */
public interface WhatsAppTypeRepository extends JpaRepository<WhatsAppType, Long> {

    Optional<WhatsAppType> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Optional<WhatsAppType> findByNameAndAccountId(
            String name,
            Integer accountId
    );

    Page<WhatsAppType> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Page<WhatsAppType> findByAccountIdAndCodeIn(
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    Page<WhatsAppType> findByNameIsContainingIgnoreCaseAndAccountId(
            String name,
            Integer accountId,
            Pageable pageable
    );

    Page<WhatsAppType> findByNameIsContainingIgnoreCaseAndAccountIdAndCodeIn(
            String name,
            Integer accountId,
            Set<String> code,
            Pageable pageable
    );

    List<WhatsAppType> findByNameStartsWithOrderByNameAsc(
            String name
    );

}
