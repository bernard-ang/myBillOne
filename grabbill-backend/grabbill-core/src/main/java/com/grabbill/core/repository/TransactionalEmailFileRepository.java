package com.grabbill.core.repository;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailFile;
import com.grabbill.core.entity.TransactionalEmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface TransactionalEmailFileRepository extends JpaRepository<TransactionalEmailFile, Long> {

    Page<TransactionalEmailFile> findByTransactionalEmailTypeAndTransactionalEmailIndexRowsIsNotEmpty(
            TransactionalEmailType transactionalEmailType,
            Pageable pageable
    );

    Page<TransactionalEmailFile> findByNameIsContainingIgnoreCaseAndTransactionalEmailTypeAndTransactionalEmailIndexRowsIsNotEmpty(
            String filename,
            TransactionalEmailType transactionalEmailType,
            Pageable pageable
    );

    List<TransactionalEmailFile> findByTransactionalEmailActivity(
            TransactionalEmailActivity transactionalEmailActivity
    );

    Optional<TransactionalEmailFile> findByNameIsIgnoreCaseAndTransactionalEmailActivity(
            String filename,
            TransactionalEmailActivity transactionalEmailActivity
    );

}
