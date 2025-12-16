package com.grabbill.server.service;

import com.grabbill.core.entity.Invoice;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.ZoneId;
import java.util.List;

/**
 * @author michaellow
 */
public interface InvoiceReportService {

    Workbook generateInvoiceReport(List<Invoice> invoices, ZoneId zoneId);

}
