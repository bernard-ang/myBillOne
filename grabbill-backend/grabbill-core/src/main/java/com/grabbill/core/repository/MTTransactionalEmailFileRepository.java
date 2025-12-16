package com.grabbill.core.repository;

import com.grabbill.core.entity.MTTransactionalEmailActivity;
import com.grabbill.core.entity.MTTransactionalEmailFile;
import com.grabbill.core.entity.MTTransactionalEmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTTransactionalEmailFileRepository extends JpaRepository<MTTransactionalEmailFile, Long> {

    Page<MTTransactionalEmailFile> findByMtTransactionalEmailTypeAndMtTransactionalEmailIndexRowsIsNotEmpty(
            MTTransactionalEmailType mtTransactionalEmailType,
            Pageable pageable
    );

    Page<MTTransactionalEmailFile> findByNameIsContainingIgnoreCaseAndMtTransactionalEmailTypeAndMtTransactionalEmailIndexRowsIsNotEmpty(
            String filename,
            MTTransactionalEmailType mtTransactionalEmailType,
            Pageable pageable
    );

    List<MTTransactionalEmailFile> findByMtTransactionalEmailActivity(
            MTTransactionalEmailActivity mtTransactionalEmailActivity
    );

    Optional<MTTransactionalEmailFile> findByNameIsIgnoreCaseAndMtTransactionalEmailActivity(
            String filename,
            MTTransactionalEmailActivity mtTransactionalEmailActivity
    );

}
