package com.grabbill.core.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author michaellow
 */
public interface InvoiceService {

    Page<Invoice> searchInvoices(
            String accountName,
            String invoiceNo,
            String planName,
            InvoiceStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Pageable pageable
    );

    List<Invoice> getInvoicesBetween(
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Set<Integer> accountIds
    );

    Optional<Invoice> getById(Integer id);

    Page<Invoice> getByAccount(Account account, Pageable pageable);

    Optional<Invoice> getByAccountAndId(Account account, Integer id);

    Optional<Invoice> getByAccountAndAccountSubscription(Account account, AccountSubscription accountSubscription);

    List<Invoice> getPaidInvoices(final Account account);

    List<Invoice> getUnpaidInvoices(Account account);

    Invoice markAsPaid(Integer id);

    Invoice markAsVoid(Integer id);

    Invoice markAsProcessing(Invoice invoice);

    Invoice markAsPaymentFailed(Invoice invoice);

    Invoice createNewTransient(
            User owner,
            BillingInformation billingInformation,
            Account account,
            AccountSubscription accountSubscription,
            double offset,
            PromoCode promoCode
    );

    Invoice createNewPersistent(
            User owner,
            com.stripe.model.Invoice stripeInvoice,
            BillingInformation billingInformation,
            Account account,
            AccountSubscription accountSubscription,
            double offset,
            PromoCode promoCode
    );

    byte[] generateInvoicePdf(
            Invoice invoice,
            com.stripe.model.Invoice stripeInvoice,
            PromoCode promoCode
    );

    Invoice createSmsTopupInvoice(
            User owner,
            com.stripe.model.Invoice stripeInvoice,
            CreditsPlanOption creditsPlanOption,
            BillingInformation billingInformation,
            Account account,
            AccountSubscription accountSubscription
    );

    byte[] generateSmsTopupInvoicePdf(
            Invoice invoice,
            com.stripe.model.Invoice stripeInvoice
    );

}
