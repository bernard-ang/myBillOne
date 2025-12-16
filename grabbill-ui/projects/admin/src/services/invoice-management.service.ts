import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiResponseModel,
  DownloadFile,
  InvoiceBasicModel,
  InvoicePaymentTransactionsPayloadModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { InvoiceManagementApi } from '../api/invoice-management.api';

@Injectable({
  providedIn: 'root',
})
export class InvoiceManagementService implements InvoiceManagementApi {
  readonly baseRoute = `mgmt/invoices`;

  constructor(private http: ApiHttpService) {}

  getInvoices(
    pageable: PageableModel,
    accountName?: string,
    invoiceNo?: string,
    planName?: string,
    status?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<InvoiceBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<InvoiceBasicModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        accountName,
        invoiceNo,
        planName,
        status,
        startDate,
        endDate,
      }
    );
  }

  getInvoice(id: number): Observable<ApiResponseModel<InvoicePaymentTransactionsPayloadModel>> {
    return this.http.get<ApiResponseModel<InvoicePaymentTransactionsPayloadModel>>(`${this.baseRoute}/${id}`);
  }

  downloadReport(
    startDate?: Date,
    endDate?: Date,
    affiliateCode?: string,
    accountName?: string
  ): Observable<DownloadFile> {
    let query = `${this.baseRoute}/reports?tz=${Intl.DateTimeFormat().resolvedOptions().timeZone}`;

    if (startDate) {
      query += `&startDate=${startDate?.toISOString()}`;
    }

    if (endDate) {
      query += `&endDate=${endDate?.toISOString()}`;
    }

    if (affiliateCode) {
      query += `&affiliateCode=${affiliateCode}`;
    }

    if (accountName) {
      query += `&account=${accountName}`;
    }

    return this.http.getBlob(query);
  }
}
