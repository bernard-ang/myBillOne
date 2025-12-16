package com.grabbill.core.repository;

import com.grabbill.core.entity.TransactionalEmailActivity;
import com.grabbill.core.entity.TransactionalEmailType;
import com.grabbill.core.model.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface TransactionalEmailActivityRepository extends JpaRepository<TransactionalEmailActivity, Long> {

    Page<TransactionalEmailActivity> findByTransactionalEmailType(TransactionalEmailType transactionalEmailType, Pageable pageable);

    Page<TransactionalEmailActivity> findByTransactionalEmailTypeAndNameIsContainingIgnoreCase(TransactionalEmailType transactionalEmailType, String name, Pageable pageable);

    Page<TransactionalEmailActivity> findByTransactionalEmailTypeAndStatus(TransactionalEmailType transactionalEmailType, ProcessStatus status, Pageable pageable);

    Page<TransactionalEmailActivity> findByTransactionalEmailTypeAndNameIsContainingIgnoreCaseAndStatus(TransactionalEmailType transactionalEmailType, String name, ProcessStatus status, Pageable pageable);

    List<TransactionalEmailActivity> findByTransactionalEmailType(TransactionalEmailType transactionalEmailType);

    List<TransactionalEmailActivity> findByTransactionalEmailTypeAndStatusIn(TransactionalEmailType transactionalEmailType, List<ProcessStatus> statuses);

    Optional<TransactionalEmailActivity> findByNameAndTransactionalEmailType(String name, TransactionalEmailType transactionalEmailType);

    Optional<TransactionalEmailActivity> findByIdAndTransactionalEmailType(Long id, TransactionalEmailType transactionalEmailType);

    List<TransactionalEmailActivity> findAllByStatusInAndPurgedTimestampIsNull(List<ProcessStatus> statuses);

    int countAllBySubmittedTimestampIsNotNull();

    List<TransactionalEmailActivity> findAllByProcessedTimestampIsNotNull();

}
