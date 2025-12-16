package com.grabbill.core.service;

import com.grabbill.core.entity.Privilege;

import java.util.List;

/**
 * @author michaellow
 */
public interface PrivilegeService {

    List<Privilege> getAll();

    List<Privilege> getByIds(List<Integer> ids);

}
