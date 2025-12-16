package com.grabbill.core.repository;

import com.grabbill.core.entity.Invoice;
import com.grabbill.core.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author michaellow
 */
public interface InvoiceRepositoryCustom {

    Page<Invoice> searchInvoices(
            String accountName,
            String invoiceNo,
            String planName,
            InvoiceStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

    List<Invoice> findInvoicesBetweenAndAccountIdIn(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Set<Integer> accountIds
    );

}
