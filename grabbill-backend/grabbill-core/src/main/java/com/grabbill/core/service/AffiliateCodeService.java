package com.grabbill.core.service;

import com.grabbill.core.entity.AffiliateCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author michaellow
 */
public interface AffiliateCodeService {

    Page<AffiliateCode> getAll(Pageable pageable);

    Page<AffiliateCode> getAllByCode(String code, Pageable pageable);

    Optional<AffiliateCode> getById(Integer id);

    Optional<AffiliateCode> getByCode(String code);

    AffiliateCode save(AffiliateCode affiliateCode);

    void delete(AffiliateCode affiliateCode);

    String generateUniqueCode();

}
