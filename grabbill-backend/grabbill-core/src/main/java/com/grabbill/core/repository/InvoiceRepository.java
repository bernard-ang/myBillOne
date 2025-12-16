package com.grabbill.core.repository;

import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.Invoice;
import com.grabbill.core.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Integer>, InvoiceRepositoryCustom {

    Page<Invoice> findByAccountId(Integer accountId, Pageable pageable);

    List<Invoice> findByAccountIdAndStatusIn(Integer accountId, Set<InvoiceStatus> statuses);

    Optional<Invoice> findByAccountIdAndId(Integer accountId, Integer id);

    Optional<Invoice> findByAccountIdAndAccountSubscription(Integer accountId, AccountSubscription accountSubscription);

}
