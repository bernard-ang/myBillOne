package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.repository.TransactionalEmailTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author michaellow
 */
public class TransactionalEmailTypeServiceImpl
        implements BaseTypeService<TransactionalEmailType, TransactionalEmailActivity> {

    @Autowired
    private TransactionalEmailTypeRepository repository;


    @Override
    public Page<TransactionalEmailType> getAll(
            final User user,
            final Pageable pageable
    ) {
        Set<String> codes = user.getUserCodes().stream().map(UserCode::getCode).collect(Collectors.toSet());
        if (codes.isEmpty()) {
            return repository.findByAccountId(user.getAccount().getId(), pageable);
        } else {
            return repository.findByAccountIdAndCodeIn(user.getAccount().getId(), codes, pageable);
        }
    }

    @Override
    public Page<TransactionalEmailType> getAllByName(
            final User user,
            final String name,
            final Pageable pageable
    ) {
        Set<String> codes = user.getUserCodes().stream().map(UserCode::getCode).collect(Collectors.toSet());
        if (codes.isEmpty()) {
            return repository.findByNameIsContainingIgnoreCaseAndAccountId(name, user.getAccount().getId(), pageable);
        } else {
            return repository.findByNameIsContainingIgnoreCaseAndAccountIdAndCodeIn(name, user.getAccount().getId(), codes, pageable);
        }
    }

    @Override
    public TransactionalEmailType getByActivity(
            final TransactionalEmailActivity transactionalEmailActivity
    ) {
        // To not allow direct retrieve by ID
        return repository.getById(transactionalEmailActivity.getTransactionalEmailType().getId());
    }

    @Override
    public Optional<TransactionalEmailType> getById(
            final User user,
            final Long id
    ) {
        return repository.findByIdAndAccountId(id, user.getAccount().getId());
    }

    @Override
    public Optional<TransactionalEmailType> getByName(
            final User user,
            final String name
    ) {
        return repository.findByNameAndAccountId(name, user.getAccount().getId());
    }

    @Override
    public TransactionalEmailType save(
            final TransactionalEmailType transactionalEmailType
    ) {
        return repository.save(transactionalEmailType);
    }

    @Override
    public void delete(
            final TransactionalEmailType transactionalEmailType
    ) {
        repository.delete(transactionalEmailType);
    }

    @Override
    public String findNextDuplicateName(
            final String transactionalEmailName
    ) {
        String name = transactionalEmailName.replaceFirst(" \\(copy \\d\\d\\)", "");
        List<TransactionalEmailType> types = repository.findByNameStartsWithOrderByNameAsc(name);
        int nextIndex = 1;
        for (TransactionalEmailType type : types) {
            if(type.getName().equals(name +  " (copy " + String.format("%02d", nextIndex) + ")")) {
                nextIndex++;
            }
        }
        return name +  " (copy " + String.format("%02d", nextIndex) + ")";
    }

}
