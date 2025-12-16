package com.grabbill.server.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.Invoice;
import com.grabbill.core.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

/**
 * @author michaellow
 */
public class AccountPaymentCheckServiceImpl implements AccountPaymentCheckService {

    @Value("${payment.settings.unpaid-grace-period.hours:336}")
    private Long unpaidInvoiceGracePeriod;

    @Autowired
    private InvoiceService invoiceService;


    @Override
    public boolean isPaymentGracePeriodOver(final Account account) {
        List<Invoice> unpaidInvoices = invoiceService.getUnpaidInvoices(account);

        if (unpaidInvoices.size() > 0) {
            unpaidInvoices.sort(Comparator.comparing(Invoice::getCycleStartDate));
            Invoice targetUnpaidInvoice = unpaidInvoices.get(unpaidInvoices.size() - 1);

            long hours = ChronoUnit.HOURS.between(targetUnpaidInvoice.getCycleStartDate(), OffsetDateTime.now(ZoneOffset.UTC));
            return hours >= unpaidInvoiceGracePeriod;
        }

        return false;
    }

}
