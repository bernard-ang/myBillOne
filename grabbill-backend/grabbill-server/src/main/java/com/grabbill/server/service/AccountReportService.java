package com.grabbill.server.service;

import com.grabbill.core.entity.Account;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author michaellow
 */
public interface AccountReportService {

    Workbook generateAccountsReport(List<Account> accounts, ZoneId zoneId);

}
