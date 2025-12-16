package com.grabbill.core.service.whatsapp;

import com.grabbill.core.entity.WhatsAppActivity;
import com.grabbill.core.entity.WhatsAppType;
import com.grabbill.core.entity.User;
import com.grabbill.core.entity.UserCode;
import com.grabbill.core.repository.WhatsAppTypeRepository;
import com.grabbill.core.service.BaseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author seez
 */
public class WhatsAppTypeServiceImpl
        implements BaseTypeService<WhatsAppType, WhatsAppActivity> {

    @Autowired
    private WhatsAppTypeRepository repository;


    @Override
    public Page<WhatsAppType> getAll(
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
    public Page<WhatsAppType> getAllByName(
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
    public WhatsAppType getByActivity(
            final WhatsAppActivity whatsAppActivity
    ) {
        // To not allow direct retrieve by ID
        return repository.getById(whatsAppActivity.getWhatsAppType().getId());
    }

    @Override
    public Optional<WhatsAppType> getById(
            final User user,
            final Long id
    ) {
        return repository.findByIdAndAccountId(id, user.getAccount().getId());
    }

    @Override
    public Optional<WhatsAppType> getByName(
            final User user,
            final String name
    ) {
        return repository.findByNameAndAccountId(name, user.getAccount().getId());
    }

    @Override
    public WhatsAppType save(
            final WhatsAppType whatsAppType
    ) {
        return repository.save(whatsAppType);
    }

    @Override
    public void delete(
            final WhatsAppType whatsAppType
    ) {
        repository.delete(whatsAppType);
    }

    @Override
    public String findNextDuplicateName(
            final String whatsAppName
    ) {
        String name = whatsAppName.replaceFirst(" \\(copy \\d\\d\\)", "");
        List<WhatsAppType> types = repository.findByNameStartsWithOrderByNameAsc(name);
        int nextIndex = 1;
        for (WhatsAppType type : types) {
            if(type.getName().equals(name +  " (copy " + String.format("%02d", nextIndex) + ")")) {
                nextIndex++;
            }
        }
        return name +  " (copy " + String.format("%02d", nextIndex) + ")";
    }

}
