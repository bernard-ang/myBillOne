package com.grabbill.server.service;

import com.grabbill.core.entity.Account;
import com.grabbill.core.entity.AccountSubscription;
import com.grabbill.core.entity.AffiliateCode;
import com.grabbill.core.service.AccountSubscriptionService;
import com.grabbill.core.service.AffiliateCodeService;
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
public class AccountReportServiceImpl extends BaseReportService implements AccountReportService {

    @Autowired
    private AccountSubscriptionService accountSubscriptionService;

    @Autowired
    private AffiliateCodeService affiliateCodeService;


    @Override
    public Workbook generateAccountsReport(
            final List<Account> accounts,
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
        headerCell.setCellValue("Account Id");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(1);
        headerCell.setCellValue("Account Name");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(2);
        headerCell.setCellValue("Account Registration Date");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(3);
        headerCell.setCellValue("Subscription Mode");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(4);
        headerCell.setCellValue("Plan");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(5);
        headerCell.setCellValue("Storage Option");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(6);
        headerCell.setCellValue("Storage Price ($)");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(7);
        headerCell.setCellValue("Trx Email Option");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(8);
        headerCell.setCellValue("Trx Email Price ($)");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(9);
        headerCell.setCellValue("Email Campaign Option");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(10);
        headerCell.setCellValue("Email Campaign Price ($)");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(11);
        headerCell.setCellValue("Total ($)");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(12);
        headerCell.setCellValue("Affiliate Name");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(13);
        headerCell.setCellValue("Affiliate Code");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(14);
        headerCell.setCellValue("Affiliate SubCode");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(15);
        headerCell.setCellValue("Subscription Start Date");
        headerCell.setCellStyle(headerCellStyle);

        headerCell = hearderRow.createCell(16);
        headerCell.setCellValue("Subscription End Date");
        headerCell.setCellStyle(headerCellStyle);

        for (Account account : accounts) {
            Row row = sheet.createRow(++rowIndex);
            Cell cell = row.createCell(0);
            cell.setCellValue(account.getId());

            cell = row.createCell(1);
            cell.setCellValue(account.getCompanyName());

            cell = row.createCell(2);
            cell.setCellValue(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(account.getCreatedDate().toInstant(), zoneId)));

            AccountSubscription accountSubscription = accountSubscriptionService.getLatestSubscriptionByAccountId(account.getId());
            if (accountSubscription != null) {
                cell = row.createCell(3);
                cell.setCellValue(accountSubscription.getMode().toString());

                cell = row.createCell(4);
                cell.setCellValue(accountSubscription.getPlanName());

                cell = row.createCell(5);
                cell.setCellValue(accountSubscription.getStorageSize() + " bytes");

                cell = row.createCell(6);
                cell.setCellValue(accountSubscription.getStoragePrice());

                cell = row.createCell(7);
                cell.setCellValue(accountSubscription.getTransactionalEmailSize() + " emails");

                cell = row.createCell(8);
                cell.setCellValue(accountSubscription.getTransactionalEmailPrice());

                cell = row.createCell(9);
                cell.setCellValue(accountSubscription.getEmailCampaignSize() + " emails");

                cell = row.createCell(10);
                cell.setCellValue(accountSubscription.getEmailCampaignPrice());

                double total = accountSubscription.getStoragePrice() + accountSubscription.getTransactionalEmailPrice() + accountSubscription.getTransactionalEmailPrice();
                cell = row.createCell(11);
                cell.setCellValue(total);

                cell = row.createCell(15);
                cell.setCellValue(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(accountSubscription.getStartDate().toInstant(), zoneId)));

                cell = row.createCell(16);
                if (accountSubscription.getEndDate() != null) {
                    cell.setCellValue(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(accountSubscription.getEndDate().toInstant(), zoneId)));
                }
            }

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
                    cell = row.createCell(12);
                    cell.setCellValue(affiliateCode.getName());

                    cell = row.createCell(13);
                    cell.setCellValue(affiliateCode.getCode());

                    if (StringUtils.hasLength(account.getAffiliateSubCode())) {
                        cell = row.createCell(14);
                        cell.setCellValue(account.getAffiliateSubCode());
                    }
                }
            }

        }

        return workbook;
    }

}
