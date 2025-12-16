import { Observable } from 'rxjs';
import {
  ApiResponseModel,
  DownloadFile,
  InvoiceBasicModel,
  InvoicePaymentTransactionsPayloadModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export abstract class InvoiceManagementApi {
  abstract getInvoices(
    pageable: PageableModel,
    accountName?: string,
    invoiceNo?: string,
    planName?: string,
    status?: string,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<InvoiceBasicModel>>>;

  abstract getInvoice(id: number): Observable<ApiResponseModel<InvoicePaymentTransactionsPayloadModel>>;

  abstract downloadReport(
    startDate?: Date,
    endDate?: Date,
    affiliateCode?: string,
    accountName?: string
  ): Observable<DownloadFile>;
}
