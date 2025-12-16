package com.grabbill.core.service;

import com.grabbill.core.entity.MTWhatsAppActivity;
import com.grabbill.core.entity.MTWhatsAppType;
import com.grabbill.core.entity.User;
import com.grabbill.core.entity.UserCode;
import com.grabbill.core.repository.MTWhatsAppTypeRepository;
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
public class MTWhatsAppTypeServiceImpl
        implements BaseTypeService<MTWhatsAppType, MTWhatsAppActivity> {

    @Autowired
    private MTWhatsAppTypeRepository repository;


    @Override
    public Page<MTWhatsAppType> getAll(
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
    public Page<MTWhatsAppType> getAllByName(
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
    public MTWhatsAppType getByActivity(
            final MTWhatsAppActivity whatsAppActivity
    ) {
        // To not allow direct retrieve by ID
        return repository.getById(whatsAppActivity.getMtWhatsAppType().getId());
    }

    @Override
    public Optional<MTWhatsAppType> getById(
            final User user,
            final Long id
    ) {
        return repository.findByIdAndAccountId(id, user.getAccount().getId());
    }

    @Override
    public Optional<MTWhatsAppType> getByName(
            final User user,
            final String name
    ) {
        return repository.findByNameAndAccountId(name, user.getAccount().getId());
    }

    @Override
    public MTWhatsAppType save(
            final MTWhatsAppType whatsAppType
    ) {
        return repository.save(whatsAppType);
    }

    @Override
    public void delete(
            final MTWhatsAppType whatsAppType
    ) {
        repository.delete(whatsAppType);
    }

    @Override
    public String findNextDuplicateName(
            final String whatsAppName
    ) {
        String name = whatsAppName.replaceFirst(" \\(copy \\d\\d\\)", "");
        List<MTWhatsAppType> types = repository.findByNameStartsWithOrderByNameAsc(name);
        int nextIndex = 1;
        for (MTWhatsAppType type : types) {
            if(type.getName().equals(name +  " (copy " + String.format("%02d", nextIndex) + ")")) {
                nextIndex++;
            }
        }
        return name +  " (copy " + String.format("%02d", nextIndex) + ")";
    }

}
