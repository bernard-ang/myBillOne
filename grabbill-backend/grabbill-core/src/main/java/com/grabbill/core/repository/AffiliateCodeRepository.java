package com.grabbill.core.repository;

import com.grabbill.core.entity.AffiliateCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface AffiliateCodeRepository extends JpaRepository<AffiliateCode, Integer> {

    Page<AffiliateCode> findAllByCodeContainingIgnoreCase(String code, Pageable pageable);

    Optional<AffiliateCode> findByCodeIs(String code);

}
