package com.grabbill.core.repository;

import com.grabbill.core.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


/**
 * @author michaellow
 */
public interface PrivilegeRepository extends JpaRepository<Privilege, Integer> {

    List<Privilege> findByIdIn(List<Integer> ids);

}
