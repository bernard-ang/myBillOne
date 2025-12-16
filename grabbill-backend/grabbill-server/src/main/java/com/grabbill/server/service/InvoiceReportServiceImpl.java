package com.grabbill.server.service;

import com.grabbill.core.entity.*;
import com.grabbill.core.model.InvoiceStatus;
import com.grabbill.core.service.AffiliateCodeService;
import com.grabbill.core.service.payment.PaymentService;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author michaellow
 */
public class InvoiceReportServiceImpl extends BaseReportService implements InvoiceReportService {

    @Autowired
    private AffiliateCodeService affiliateCodeService;

    @Autowired
    private PaymentService paymentService;


    @Override
    public Workbook generateInvoiceReport(
            final List<Invoice> invoices,
            final ZoneId zoneId
    ) {
        Map<Integer, AffiliateCode> accountIdToAffiliateCodeMap = new HashMap<>();
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("invoices");
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 6000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 6000);
        sheet.setColumnWidth(4, 6000);
        sheet.setColumnWidth(5, 6000);
        sheet.setColumnWidth(6, 6000);
        sheet.setColumnWidth(7, 6000);
        sheet.setColumnWidth(8, 6000);
        sheet.setColumnWidth(9, 6000);
        sheet.setColumnWidth(10, 6000);
        sheet.setColumnWidth(11, 6000);
        sheet.setColumnWidth(12, 6000);
        sheet.setColumnWidth(13, 6000);
        sheet.setColumnWidth(14, 6000);
        sheet.setColumnWidth(15, 6000);
        sheet.setColumnWidth(16, 6000);
        CellStyle headerCellStyle = createHeaderCellStyle(workbook);

        int rowIndex = 0;
        Row hearderRow = sheet.createRow(rowIndex);
        Cell headerCell = hearderRow.createCell(0);
        headerCell.setCellValue("Invoice Id");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(1);
        headerCell.setCellValue("Invoice No");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(2);
        headerCell.setCellValue("Account Id");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(3);
        headerCell.setCellValue("Account Registration Date");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(4);
        headerCell.setCellValue("Affiliate Name");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(5);
        headerCell.setCellValue("Affiliate Code");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(6);
        headerCell.setCellValue("Affiliate SubCode");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(7);
        headerCell.setCellValue("Biller Name");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(8);
        headerCell.setCellValue("Plan Name");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(9);
        headerCell.setCellValue("Start Date");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(10);
        headerCell.setCellValue("End Date");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(11);
        headerCell.setCellValue("Credit Card Type");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(12);
        headerCell.setCellValue("Credit Card No");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(13);
        headerCell.setCellValue("Issuer");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(14);
        headerCell.setCellValue("Total Amount");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(15);
        headerCell.setCellValue("Status");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(16);
        headerCell.setCellValue("Remarks");
        headerCell.setCellStyle(headerCellStyle);

        for (Invoice invoice : invoices) {

            Row row = sheet.createRow(++rowIndex);
            Cell cell = row.createCell(0);
            cell.setCellValue(invoice.getId());

            cell = row.createCell(1);
            cell.setCellValue(invoice.getInvoiceNo());

            Account account = invoice.getAccount();
            cell = row.createCell(2);
            cell.setCellValue(account.getId());

            cell = row.createCell(3);
            cell.setCellValue(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(account.getCreatedDate().toInstant(), zoneId)));

            if (StringUtils.hasLength(account.getAffiliateMasterCode())) {
                AffiliateCode affiliateCode = accountIdToAffiliateCodeMap.get(account.getId());
                if (affiliateCode == null) {
                    Optional<AffiliateCode> affiliateCodeOptional = affiliateCodeService.getByCode(account.getAffiliateMasterCode());
                    if (affiliateCodeOptional.isPresent()) {
                        affiliateCode = affiliateCodeOptional.get();
                        accountIdToAffiliateCodeMap.put(account.getId(), affiliateCode);
                    }
                }

                if (affiliateCode != null) {
                    cell = row.createCell(4);
                    cell.setCellValue(affiliateCode.getName());

                    cell = row.createCell(5);
                    cell.setCellValue(affiliateCode.getCode());

                    if (StringUtils.hasLength(account.getAffiliateSubCode())) {
                        cell = row.createCell(6);
                        cell.setCellValue(account.getAffiliateSubCode());
                    }
                }
            }

            cell = row.createCell(7);
            cell.setCellValue(invoice.getBillToName());

            cell = row.createCell(8);
            cell.setCellValue(invoice.getPlanName());

            cell = row.createCell(9);
            cell.setCellValue(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(invoice.getCycleStartDate().toInstant(), zoneId)));

            cell = row.createCell(10);
            cell.setCellValue(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(invoice.getCycleEndDate().toInstant(), zoneId)));

            cell = row.createCell(14);
            cell.setCellValue(invoice.getTotalAmountWithTax());

            cell = row.createCell(15);
            cell.setCellValue(invoice.getStatus().toString());

            // NOTE: backward compatibility support, old invoices do not have this stripeInvoiceId
            if (StringUtils.hasLength(invoice.getStripeInvoiceId())) {
                if (InvoiceStatus.PAID.equals(invoice.getStatus())) {
                    PaymentMethod paymentMethod = paymentService.getPaymentMethodByInvoiceId(invoice.getStripeInvoiceId());
                    if (paymentMethod != null) {

                        PaymentMethod.Card card = paymentMethod.getCard();
                        if (card != null) {
                            cell = row.createCell(11);
                            cell.setCellValue(card.getBrand());

                            cell = row.createCell(12);
                            cell.setCellValue(card.getLast4());

                            cell = row.createCell(13);
                            cell.setCellValue(card.getIssuer());
                        }
                    }

                } else if (InvoiceStatus.PAYMENT_FAILED.equals(invoice.getStatus())) {
                    PaymentIntent paymentIntent = paymentService.getPaymentIntentByInvoiceId(invoice.getStripeInvoiceId());
                    cell = row.createCell(16);
                    cell.setCellValue(paymentIntent.getStatus());
                }
            }
        }

        return workbook;
    }

}
