package com.grabbill.core.repository;

import com.grabbill.core.entity.DigitalFilingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface DigitalFilingTypeRepository extends JpaRepository<DigitalFilingType, Long> {

    Optional<DigitalFilingType> findByIdAndAccountId(
            Long id,
            Integer accountId
    );

    Optional<DigitalFilingType> findByNameAndAccountId(
            String name,
            Integer accountId
    );

    Page<DigitalFilingType> findByAccountId(
            Integer accountId,
            Pageable pageable
    );

    Page<DigitalFilingType> findByAccountIdAndCodeIn(
            Integer accountId,
            Set<String> codes,
            Pageable pageable
    );

    Page<DigitalFilingType> findByNameIsContainingIgnoreCaseAndAccountId(
            String name,
            Integer accountId,
            Pageable pageable
    );

    Page<DigitalFilingType> findByNameIsContainingIgnoreCaseAndAccountIdAndCodeIn(
            String name,
            Integer accountId,
            Set<String> codes,
            Pageable pageable
    );

    List<DigitalFilingType> findByNameStartsWithOrderByNameAsc(String name);

}
