package com.grabbill.core.service;

import com.grabbill.core.entity.CreditsPlanOption;
import com.grabbill.core.model.CreditType;
import com.grabbill.core.repository.CreditsPlanOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public class CreditsPlanOptionServiceImpl implements CreditsPlanOptionService {

    @Autowired
    private CreditsPlanOptionRepository repository;


    @Override
    public List<CreditsPlanOption> getByType(final CreditType type) {
        return repository.findByTypeIs(type);
    }

    @Override
    public Optional<CreditsPlanOption> getById(final Integer id) {
        return repository.findById(id);
    }

}
