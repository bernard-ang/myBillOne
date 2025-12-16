package com.grabbill.core.service;

import com.grabbill.core.entity.PromoCode;
import com.grabbill.core.repository.PromoCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * @author michaellow
 */
public class PromoCodeServiceImpl implements PromoCodeService {

    @Autowired
    private PromoCodeRepository repository;


    @Override
    public Page<PromoCode> getAll(final Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<PromoCode> getAllByCode(final String code, final Pageable pageable) {
        return repository.findAllByCodeContainingIgnoreCase(code, pageable);
    }

    @Override
    public Optional<PromoCode> getById(final Integer id) {
        return repository.findById(id);
    }

    @Override
    public Optional<PromoCode> getByCode(final String code) {
        return repository.findByCodeIs(code);
    }

    @Override
    public PromoCode save(PromoCode promoCode) {
        return repository.save(promoCode);
    }

    @Override
    public void delete(PromoCode promoCode) {
        repository.deleteById(promoCode.getId());
    }

}
