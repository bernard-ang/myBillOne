package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.repository.SmsTypeRepository;
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
public class SmsTypeServiceImpl
        implements BaseTypeService<SmsType, SmsActivity> {

    @Autowired
    private SmsTypeRepository repository;


    @Override
    public Page<SmsType> getAll(
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
    public SmsType getByActivity(
            final SmsActivity smsActivity
    ) {
        // To not allow direct retrieve by ID
        return repository.getById(smsActivity.getSmsType().getId());
    }

    @Override
    public Page<SmsType> getAllByName(
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
    public Optional<SmsType> getById(
            final User user,
            final Long id
    ) {
        return repository.findByIdAndAccountId(id, user.getAccount().getId());
    }

    @Override
    public Optional<SmsType> getByName(
            final User user,
            final String name
    ) {
        return repository.findByNameAndAccountId(name, user.getAccount().getId());
    }

    @Override
    public SmsType save(
            final SmsType smsType
    ) {
        return repository.save(smsType);
    }

    @Override
    public void delete(
            final SmsType smsType
    ) {
        repository.delete(smsType);
    }

    @Override
    public String findNextDuplicateName(
            final String smsTypeName
    ) {
        String name = smsTypeName.replaceFirst(" \\(copy \\d\\d\\)", "");
        List<SmsType> types = repository.findByNameStartsWithOrderByNameAsc(name);
        int nextIndex = 1;
        for (SmsType type : types) {
            if(type.getName().equals(name +  " (copy " + String.format("%02d", nextIndex) + ")")) {
                nextIndex++;
            }
        }
        return name +  " (copy " + String.format("%02d", nextIndex) + ")";
    }

}
