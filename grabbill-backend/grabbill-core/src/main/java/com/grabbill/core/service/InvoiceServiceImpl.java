package com.grabbill.core.service;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.collect.Sets;
import com.grabbill.core.entity.*;
import com.grabbill.core.exception.GrabbillException;
import com.grabbill.core.model.DiscountType;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.model.InvoiceType;
import com.grabbill.core.model.SubscriptionMode;
import com.grabbill.core.repository.InvoiceRepository;
import com.grabbill.core.utils.PaymentUtils;
import com.itextpdf.html2pdf.HtmlConverter;
import com.stripe.model.Coupon;
import com.stripe.model.InvoiceLineItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author michaellow
 */
public class InvoiceServiceImpl implements InvoiceService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;
    @Autowired
    private InvoiceRepository repository;
    @Autowired
    private TaxService taxService;


    @Override
    public Page<Invoice> searchInvoices(
            final String accountName,
            final String invoiceNo,
            final String planName,
            final InvoiceStatus status,
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Pageable pageable
    ) {
        return repository.searchInvoices(accountName, invoiceNo, planName, status, startDate, endDate, pageable);
    }

    @Override
    public List<Invoice> getInvoicesBetween(
            final OffsetDateTime startDate,
            final OffsetDateTime endDate,
            final Set<Integer> accountIds
    ) {
        return repository.findInvoicesBetweenAndAccountIdIn(startDate, endDate, accountIds);
    }

    @Override
    public Optional<Invoice> getById(final Integer id) {
        return repository.findById(id);
    }

    @Override
    public Page<Invoice> getByAccount(
            final Account account,
            final Pageable pageable
    ) {
        return repository.findByAccountId(account.getId(), pageable);
    }

    @Override
    public Optional<Invoice> getByAccountAndId(
            final Account account,
            final Integer id
    ) {
        return repository.findByAccountIdAndId(account.getId(), id);
    }

    @Override
    public Optional<Invoice> getByAccountAndAccountSubscription(Account account, AccountSubscription accountSubscription) {
        return repository.findByAccountIdAndAccountSubscription(account.getId(), accountSubscription);
    }

    @Override
    public List<Invoice> getUnpaidInvoices(final Account account) {
        return repository.findByAccountIdAndStatusIn(
                account.getId(),
                Sets.newHashSet(InvoiceStatus.NEW, InvoiceStatus.PAYMENT_FAILED)
        );
    }

    @Override
    public List<Invoice> getPaidInvoices(final Account account) {
        return repository.findByAccountIdAndStatusIn(
                account.getId(),
                Sets.newHashSet(InvoiceStatus.PAID)
        );
    }

    @Override
    public Invoice markAsPaid(final Integer id) {
        Optional<Invoice> targetOptional = repository.findById(id);
        if (targetOptional.isPresent()) {
            Invoice target = targetOptional.get();
            target.setStatus(InvoiceStatus.PAID);

            return repository.save(target);
        }

        return null;
    }

    @Override
    public Invoice markAsVoid(final Integer id) {
        Optional<Invoice> targetOptional = repository.findById(id);
        if (targetOptional.isPresent()) {
            Invoice target = targetOptional.get();
            target.setStatus(InvoiceStatus.VOID);

            return repository.save(target);
        }

        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Invoice markAsProcessing(final Invoice invoice) {
        invoice.setStatus(InvoiceStatus.PROCESSING);
        return repository.saveAndFlush(invoice);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Invoice markAsPaymentFailed(final Invoice invoice) {
        invoice.setStatus(InvoiceStatus.PAYMENT_FAILED);
        return repository.saveAndFlush(invoice);
    }

    @Override
    public Invoice createNewTransient(
            final User owner,
            final BillingInformation billingInformation,
            final Account account,
            final AccountSubscription accountSubscription,
            final double offsetAmount,
            final PromoCode promoCode
    ) {
        return createInternal(owner, billingInformation, account, accountSubscription, offsetAmount, promoCode);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public Invoice createNewPersistent(
            final User owner,
            final com.stripe.model.Invoice stripeInvoice,
            final BillingInformation billingInformation,
            final Account account,
            final AccountSubscription accountSubscription,
            final double offsetAmount,
            final PromoCode promoCode
    ) {
        Invoice newInstance = createInternal(owner, billingInformation, account, accountSubscription, offsetAmount, promoCode);
        if (stripeInvoice != null) {
            newInstance.setStripeInvoiceId(stripeInvoice.getId());
            newInstance.setInvoiceNo(stripeInvoice.getNumber());

            if ("paid".equalsIgnoreCase(stripeInvoice.getStatus())) {
                newInstance.setStatus(InvoiceStatus.PAID);
            } else if ("open".equalsIgnoreCase(stripeInvoice.getStatus())) {
                newInstance.setStatus(InvoiceStatus.PAYMENT_FAILED);
            } else if ("void".equalsIgnoreCase(stripeInvoice.getStatus())) {
                newInstance.setStatus(InvoiceStatus.VOID);
            }
        }
        return repository.save(newInstance);
    }

    private Invoice createInternal(
            final User owner,
            final BillingInformation billingInformation,
            final Account account,
            final AccountSubscription accountSubscription,
            final double offsetAmount,
            final PromoCode promoCode
    ) {
        Invoice newInstance = new Invoice();
        newInstance.setPlanName(accountSubscription.getPlanName());
        newInstance.setPlanDescription(accountSubscription.getPlanDescription());
        newInstance.setCycleStartDate(accountSubscription.getCycleStartDate());
        newInstance.setStorageSize(accountSubscription.getStorageSize());
        newInstance.setEmailCampaignSize(accountSubscription.getEmailCampaignSize());
        newInstance.setTransactionalEmailSize(accountSubscription.getTransactionalEmailSize());

        // append billing info
        if (billingInformation != null) {
            newInstance.setBillToName(billingInformation.getName());
            newInstance.setBillToContactNo(billingInformation.getContactNo());
            newInstance.setBillToEmail(billingInformation.getEmail());
            newInstance.setBillToAddrLine1(billingInformation.getAddrLine1());
            newInstance.setBillToAddrLine2(billingInformation.getAddrLine2());
            newInstance.setBillToCity(billingInformation.getCity());
            newInstance.setBillToState(billingInformation.getState());
            newInstance.setBillToPostcode(billingInformation.getPostcode());
            newInstance.setBillToCountry(billingInformation.getCountry());

        } else {
            newInstance.setBillToName(owner.getName());
            newInstance.setBillToContactNo(account.getCompanyContactNo());
            newInstance.setBillToEmail(owner.getEmail());
            newInstance.setBillToAddrLine1(account.getAddrLine1());
            newInstance.setBillToAddrLine2(account.getAddrLine2());
            newInstance.setBillToCity(account.getCity());
            newInstance.setBillToState(account.getState());
            newInstance.setBillToPostcode(account.getPostcode());
            newInstance.setBillToCountry(account.getCountry());
        }

        newInstance.setStoragePrice(accountSubscription.getStoragePrice());
        newInstance.setEmailCampaignPrice(accountSubscription.getEmailCampaignPrice());
        newInstance.setTransactionalEmailPrice(accountSubscription.getTransactionalEmailPrice());
        newInstance.setSmsTopupSize(0L);
        newInstance.setSmsTopupPrice(0d);
        newInstance.setDiscountAmount(0d);
        newInstance.setOverdueAmount(0d);
        newInstance.setOffsetAmount(offsetAmount);
        newInstance.setTotalTaxPercentage(taxService.getSstRate());

        if (SubscriptionMode.MONTHLY.equals(accountSubscription.getMode())) {
            newInstance.setCycleEndDate(accountSubscription.getCycleEndDate());
            newInstance.setTotalAmount(accountSubscriptionService.getMonthlyRate(accountSubscription));

        } else {
            // NOTE: different invoice period based on subscription mode - ANNUALLY
            newInstance.setCycleEndDate(accountSubscription.getCycleStartDate().plusYears(1));
            newInstance.setTotalAmount(accountSubscriptionService.getAnnuallyRate(accountSubscription));
        }

        // apply promo code discount
        if (promoCode != null) {
            double totalAmountAfterOffset = newInstance.getTotalAmount() - newInstance.getOffsetAmount();
            double discountAmount = 0;
            if (DiscountType.ABSOLUTE_AMOUNT.equals(promoCode.getDiscountType())) {
                discountAmount = promoCode.getDiscount();

            } else if (DiscountType.PERCENTAGE.equals(promoCode.getDiscountType())) {
                discountAmount = totalAmountAfterOffset * (promoCode.getDiscount() / 100d);
            }

            double totalAmountAfterDiscount = totalAmountAfterOffset > discountAmount ?
                    (totalAmountAfterOffset - discountAmount) : 0;
            newInstance.setTotalAmount(totalAmountAfterDiscount);
            newInstance.setDiscountAmount(discountAmount);
        } else {
            double totalAmountAfterOffset = newInstance.getTotalAmount() - newInstance.getOffsetAmount();
            newInstance.setTotalAmount(totalAmountAfterOffset);
        }

        if (newInstance.getTotalAmount() > 0) {
            double totalAmountAfterOffset = newInstance.getTotalAmount();
            newInstance.setTotalTaxAmount(totalAmountAfterOffset * (taxService.getSstRate() / 100));
            newInstance.setTotalAmountWithTax(totalAmountAfterOffset + newInstance.getTotalTaxAmount());

            if (newInstance.getTotalAmountWithTax() <= 0) {
                newInstance.setTotalTaxAmount(0d);
                newInstance.setTotalAmountWithTax(0d);
            }

            if (newInstance.getTotalAmountWithTax() == 0) {
                newInstance.setStatus(InvoiceStatus.PAID);
            } else {
                newInstance.setStatus(InvoiceStatus.NEW);
            }

        // total amount == 0, free plan
        } else {
            newInstance.setTotalTaxAmount(0d);
            newInstance.setTotalAmountWithTax(0d);
            newInstance.setStatus(InvoiceStatus.PAID);
        }

        // append references
        newInstance.setAccount(account);
        newInstance.setAccountSubscription(accountSubscription);
        newInstance.setType(InvoiceType.PLAN);

        return newInstance;
    }

    @Override
    public byte[] generateInvoicePdf(
            final Invoice invoice,
            final com.stripe.model.Invoice stripeInvoice,
            final PromoCode promoCode
    ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile("invoice");

            Map<String, String> params = new HashMap<>();
            params.put("invoiceNo", invoice.getInvoiceNo());
            params.put("dateIssued", DATE_FORMATTER.format(invoice.getCreatedDate()));
            params.put("dateDue", DATE_FORMATTER.format(invoice.getCreatedDate()));
            params.put("billTo", invoice.getBillToEmail());
            params.put("totalAmount", "RM " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(stripeInvoice.getTotal() / 100d)));
            params.put("cycleStart", DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(invoice.getCycleStartDate().toInstant(), ZoneId.of("Asia/Kuala_Lumpur"))));
            params.put("cycleEnd", DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(invoice.getCycleEndDate().toInstant(), ZoneId.of("Asia/Kuala_Lumpur"))));

            // product line 1
            InvoiceLineItem product1 = stripeInvoice.getLines().getData().get(0);
            params.put("product1Name", product1.getDescription());
            params.put("product1UnitPrice", "RM " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(product1.getPrice().getUnitAmount() / 100d)));
            params.put("product1Amount", "RM " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(product1.getPrice().getUnitAmount() / 100d)));

            // product line 2
            InvoiceLineItem product2 = stripeInvoice.getLines().getData().get(1);
            params.put("product2Name", product2.getDescription());
            params.put("product2UnitPrice", "RM " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(product2.getPrice().getUnitAmount() / 100d)));
            params.put("product2Amount", "RM " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(product2.getPrice().getUnitAmount() / 100d)));

            // product line 3
            InvoiceLineItem product3 = stripeInvoice.getLines().getData().get(2);
            params.put("product3Name", product3.getDescription());
            params.put("product3UnitPrice", "RM  " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(product3.getPrice().getUnitAmount() / 100d)));
            params.put("product3Amount", "RM  " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(product3.getPrice().getUnitAmount() / 100d)));

            params.put("subTotal", "RM  " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(stripeInvoice.getSubtotal() / 100d)));

            if (invoice.getAccountSubscription().getMode() == SubscriptionMode.ANNUALLY) {
                params.put("subscriptionModeDiscount", "Annual Discount (10%)");
                params.put("subscriptionModeDiscountTotal", "RM -" + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(stripeInvoice.getSubtotal() * 0.1 / 100d)));
            }

            if (invoice.getOffsetAmount() > 0) {
                params.put("prorated", "Prorated Offset");
                params.put("offsetTotal", "RM -" + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(invoice.getOffsetAmount())));
            }

            if (promoCode != null && promoCode.isActive()) {
                params.put("promoCode", promoCode.getCode() + " (" + promoCode.getName() + ")");
                params.put("promoTotal", "RM -" + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(invoice.getDiscountAmount())));
            }

            params.put("total", "RM  " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(stripeInvoice.getTotal() / 100d)));
            params.put("totalAmountDue", "RM  " + PaymentUtils.decimalFormatPaymentAmount(BigDecimal.valueOf(stripeInvoice.getTotal() / 100d)));

            HtmlConverter.convertToPdf(template.apply(params), baos);

        } catch (IOException e) {
            throw new GrabbillException("Failed to generate invoice pdf", e);
        }

        return baos.toByteArray();
    }

    @Override
    public Invoice createSmsTopupInvoice(
            final User owner,
            final com.stripe.model.Invoice stripeInvoice,
            final CreditsPlanOption creditsPlanOption,
            final BillingInformation billingInformation,
            final Account account,
            final AccountSubscription accountSubscription
    ) {
        Invoice newInstance = new Invoice();

        // append billing info
        if (billingInformation != null) {
            newInstance.setBillToName(billingInformation.getName());
            newInstance.setBillToContactNo(billingInformation.getContactNo());
            newInstance.setBillToEmail(billingInformation.getEmail());
            newInstance.setBillToAddrLine1(billingInformation.getAddrLine1());
            newInstance.setBillToAddrLine2(billingInformation.getAddrLine2());
            newInstance.setBillToCity(billingInformation.getCity());
            newInstance.setBillToState(billingInformation.getState());
            newInstance.setBillToPostcode(billingInformation.getPostcode());
            newInstance.setBillToCountry(billingInformation.getCountry());

        } else {
            newInstance.setBillToName(owner.getName());
            newInstance.setBillToContactNo(account.getCompanyContactNo());
            newInstance.setBillToEmail(owner.getEmail());
            newInstance.setBillToAddrLine1(account.getAddrLine1());
            newInstance.setBillToAddrLine2(account.getAddrLine2());
            newInstance.setBillToCity(account.getCity());
            newInstance.setBillToState(account.getState());
            newInstance.setBillToPostcode(account.getPostcode());
            newInstance.setBillToCountry(account.getCountry());
        }

        newInstance.setPlanName(stripeInvoice.getLines().getData().get(0).getDescription());
        newInstance.setCycleStartDate(OffsetDateTime.ofInstant(Instant.ofEpochSecond(stripeInvoice.getPeriodStart()), ZoneOffset.UTC));
        newInstance.setCycleEndDate(OffsetDateTime.ofInstant(Instant.ofEpochSecond(stripeInvoice.getPeriodEnd()), ZoneOffset.UTC));
        newInstance.setStorageSize(0L);
        newInstance.setEmailCampaignSize(0L);
        newInstance.setTransactionalEmailSize(0L);
        newInstance.setSmsTopupSize(Long.valueOf(creditsPlanOption.getQuantity()));
        newInstance.setStoragePrice(0d);
        newInstance.setEmailCampaignPrice(0d);
        newInstance.setTransactionalEmailPrice(0d);
        newInstance.setSmsTopupPrice(creditsPlanOption.getPrice());
        newInstance.setOverdueAmount(0d);
        newInstance.setOffsetAmount(0d);
        newInstance.setDiscountAmount(0d);
        newInstance.setTotalTaxPercentage(taxService.getSstRate());
        newInstance.setTotalAmount(BigDecimal.valueOf(stripeInvoice.getTotal() / 100d).doubleValue());
        newInstance.setTotalTaxAmount(newInstance.getTotalAmount() * (taxService.getSstRate() / 100));
        newInstance.setTotalAmountWithTax(newInstance.getTotalAmount() + newInstance.getTotalTaxAmount());
        newInstance.setStatus(InvoiceStatus.PAID);
        newInstance.setInvoiceNo(stripeInvoice.getNumber());
        newInstance.setType(InvoiceType.TOPUP);

        // append references
        newInstance.setAccount(account);
        newInstance.setAccountSubscription(accountSubscription);

        return repository.save(newInstance);
    }

    @Override
    public byte[] generateSmsTopupInvoicePdf(
            final Invoice invoice,
            final com.stripe.model.Invoice stripeInvoice
    ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
            Handlebars handlebars = new Handlebars(loader);
            Template template = handlebars.compile("invoice-credits-topup");

            Map<String, String> params = new HashMap<>();
            params.put("invoiceNo", invoice.getInvoiceNo());
            params.put("dateIssued", DATE_FORMATTER.format(invoice.getCreatedDate()));
            params.put("billTo", invoice.getBillToEmail());
            params.put("totalAmount", "RM " + + stripeInvoice.getTotal());

            // product line 1
            InvoiceLineItem product1 = stripeInvoice.getLines().getData().get(0);
            params.put("product1Name", product1.getDescription());
            params.put("product1UnitPrice", "RM " + product1.getPrice().getUnitAmountDecimal().divide(BigDecimal.valueOf(100)));
            params.put("product1Amount", "RM " + product1.getPrice().getUnitAmountDecimal().divide(BigDecimal.valueOf(100)));
            if (!product1.getDiscounts().isEmpty()) {
                params.put("product1Discount", product1.getDiscounts().get(0));
                params.put("product1DiscountAmount", "RM -" + BigDecimal.valueOf(product1.getDiscountAmounts().get(0).getAmount()).divide(BigDecimal.valueOf(100)));
            }

            params.put("subTotal", "RM " + BigDecimal.valueOf(stripeInvoice.getSubtotal()).divide(BigDecimal.valueOf(100)));
            params.put("total", "RM " + BigDecimal.valueOf(stripeInvoice.getTotal()).divide(BigDecimal.valueOf(100)));
            params.put("totalAmountDue", "RM " + BigDecimal.valueOf(stripeInvoice.getTotal()).divide(BigDecimal.valueOf(100)));

            HtmlConverter.convertToPdf(template.apply(params), baos);

        } catch (IOException e) {
            throw new GrabbillException("Failed to generate sms credits topup invoice pdf", e);
        }

        return baos.toByteArray();
    }

}
