package com.grabbill.core.repository;

import com.grabbill.core.entity.PromoCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface PromoCodeRepository extends JpaRepository<PromoCode, Integer> {

    Page<PromoCode> findAllByCodeContainingIgnoreCase(String code, Pageable pageable);

    Optional<PromoCode> findByCodeIs(String code);

}
