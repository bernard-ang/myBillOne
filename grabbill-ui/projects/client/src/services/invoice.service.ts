import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  InvoiceBasicModel,
  InvoiceModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { InvoiceApi } from '../api/invoice.api';

@Injectable({
  providedIn: 'root',
})
export class InvoiceService implements InvoiceApi {
  readonly baseRoute = `invoices`;

  constructor(private http: ApiHttpService) {}

  getInvoices(pageable: PageableModel): Observable<ApiResponseModel<SearchResultPayloadModel<InvoiceBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<InvoiceBasicModel>>>(
      `${this.baseRoute}`,
      pageable
    );
  }

  getInvoice(id: number): Observable<ApiResponseModel<InvoiceModel>> {
    return this.http.get<ApiResponseModel<InvoiceModel>>(`${this.baseRoute}/${id}`);
  }

  getInvoiceByAccountSubscriptionId (accountSubscriptionId: number): Observable<ApiResponseModel<InvoiceModel>> {
    return this.http.get<ApiResponseModel<InvoiceModel>>(`${this.baseRoute}/subscription/${accountSubscriptionId}`);
  }

  retryInvoicePayment(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}/pay`, {});
  }
}
