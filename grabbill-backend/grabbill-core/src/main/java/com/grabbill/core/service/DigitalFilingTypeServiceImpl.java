package com.grabbill.core.service;

import com.grabbill.core.entity.DigitalFilingActivity;
import com.grabbill.core.entity.DigitalFilingType;
import com.grabbill.core.entity.User;
import com.grabbill.core.entity.UserCode;
import com.grabbill.core.repository.DigitalFilingTypeRepository;
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
public class DigitalFilingTypeServiceImpl implements BaseTypeService<DigitalFilingType, DigitalFilingActivity> {

    @Autowired
    private DigitalFilingTypeRepository repository;


    @Override
    public Page<DigitalFilingType> getAll(
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
    public Page<DigitalFilingType> getAllByName(
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
    public DigitalFilingType getByActivity(final DigitalFilingActivity digitalFilingActivity) {
        // To not allow direct retrieve by ID
        return repository.getById(digitalFilingActivity.getDigitalFilingType().getId());
    }

    @Override
    public Optional<DigitalFilingType> getById(
            final User user,
            final Long id
    ) {
        return repository.findByIdAndAccountId(id, user.getAccount().getId());
    }

    @Override
    public Optional<DigitalFilingType> getByName(User user, String name) {
        return repository.findByNameAndAccountId(name, user.getAccount().getId());
    }

    @Override
    public DigitalFilingType save(
            final DigitalFilingType digitalFilingType
    ) {
        return repository.save(digitalFilingType);
    }

    @Override
    public void delete(
            final DigitalFilingType digitalFilingType
    ) {
        repository.delete(digitalFilingType);
    }

    @Override
    public String findNextDuplicateName(String digitalFilingName) {
        String name = digitalFilingName.replaceAll(" \\(copy \\d\\d\\)", "");
        List<DigitalFilingType> types = repository.findByNameStartsWithOrderByNameAsc(name);
        int nextIndex = 1;
        for (DigitalFilingType type : types) {
            if(type.getName().equals(name +  " (copy " + String.format("%02d", nextIndex) + ")")) {
                nextIndex++;
            }
        }
        return name +  " (copy " + String.format("%02d", nextIndex) + ")";
    }

}
