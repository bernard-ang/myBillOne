package com.grabbill.core.repository;

import com.grabbill.core.entity.MTTransactionalEmailActivity;
import com.grabbill.core.entity.MTTransactionalEmailType;
import com.grabbill.core.model.ProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author michaellow
 */
public interface MTTransactionalEmailActivityRepository extends JpaRepository<MTTransactionalEmailActivity, Long> {

    Page<MTTransactionalEmailActivity> findByMtTransactionalEmailType(MTTransactionalEmailType mtTransactionalEmailType, Pageable pageable);

    Page<MTTransactionalEmailActivity> findByMtTransactionalEmailTypeAndNameIsContainingIgnoreCase(MTTransactionalEmailType mtTransactionalEmailType, String name, Pageable pageable);

    Page<MTTransactionalEmailActivity> findByMtTransactionalEmailTypeAndStatus(MTTransactionalEmailType mtTransactionalEmailType, ProcessStatus status, Pageable pageable);

    Page<MTTransactionalEmailActivity> findByMtTransactionalEmailTypeAndNameIsContainingIgnoreCaseAndStatus(MTTransactionalEmailType mtTransactionalEmailType, String name, ProcessStatus status, Pageable pageable);

    List<MTTransactionalEmailActivity> findByMtTransactionalEmailType(MTTransactionalEmailType mtTransactionalEmailType);

    List<MTTransactionalEmailActivity> findByMtTransactionalEmailTypeAndStatusIn(MTTransactionalEmailType mtTransactionalEmailType, List<ProcessStatus> statuses);

    Optional<MTTransactionalEmailActivity> findByNameAndMtTransactionalEmailType(String name, MTTransactionalEmailType mtTransactionalEmailType);

    Optional<MTTransactionalEmailActivity> findByIdAndMtTransactionalEmailType(Long id, MTTransactionalEmailType mtTransactionalEmailType);

    List<MTTransactionalEmailActivity> findAllByStatusInAndPurgedTimestampIsNull(List<ProcessStatus> statuses);

    int countAllBySubmittedTimestampIsNotNull();

    List<MTTransactionalEmailActivity> findAllByProcessedTimestampIsNotNull();

}
