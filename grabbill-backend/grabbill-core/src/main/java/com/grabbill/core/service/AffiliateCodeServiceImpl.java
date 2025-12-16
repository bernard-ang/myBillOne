package com.grabbill.core.service;

import com.grabbill.core.entity.AffiliateCode;
import com.grabbill.core.repository.AffiliateCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author michaellow
 */
public class AffiliateCodeServiceImpl implements AffiliateCodeService {

    private static final String AFC_FORMAT = "A%04d";

    @Autowired
    private AffiliateCodeRepository repository;


    @Override
    public Page<AffiliateCode> getAll(final Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<AffiliateCode> getAllByCode(final String code, final Pageable pageable) {
        return repository.findAllByCodeContainingIgnoreCase(code, pageable);
    }

    @Override
    public Optional<AffiliateCode> getById(final Integer id) {
        return repository.findById(id);
    }

    @Override
    public Optional<AffiliateCode> getByCode(final String code) {
        return repository.findByCodeIs(code);
    }

    @Override
    public AffiliateCode save(final AffiliateCode affiliateCode) {
        return repository.save(affiliateCode);
    }

    @Override
    public void delete(final AffiliateCode affiliateCode) {
        repository.delete(affiliateCode);
    }

    @Override
    public String generateUniqueCode() {
        long totalRecords = repository.count();

        long index = totalRecords;
        String uniqueCode = null;
        while (uniqueCode == null) {
            String tempCode = String.format(AFC_FORMAT, index);
            if (repository.findByCodeIs(tempCode).isEmpty()) {
                uniqueCode = tempCode;
            }
            index++;
        }
        return uniqueCode;
    }

}
