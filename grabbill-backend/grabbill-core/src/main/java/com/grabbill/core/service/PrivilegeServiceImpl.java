package com.grabbill.core.service;

import com.grabbill.core.entity.Privilege;
import com.grabbill.core.repository.PrivilegeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author michaellow
 */
public class PrivilegeServiceImpl implements PrivilegeService {

    @Autowired
    private PrivilegeRepository repository;


    @Override
    public List<Privilege> getAll() {
        return repository.findAll();
    }

    @Override
    public List<Privilege> getByIds(final List<Integer> ids) {
        return repository.findByIdIn(ids);
    }

}
