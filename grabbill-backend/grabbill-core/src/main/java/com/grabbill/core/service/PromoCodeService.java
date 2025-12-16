package com.grabbill.core.service;

import com.grabbill.core.entity.PromoCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface PromoCodeService {

    Page<PromoCode> getAll(Pageable pageable);

    Page<PromoCode> getAllByCode(String code, Pageable pageable);

    Optional<PromoCode> getById(Integer id);

    Optional<PromoCode> getByCode(String code);

    PromoCode save(PromoCode promoCode);

    void delete(PromoCode promoCode);

}
