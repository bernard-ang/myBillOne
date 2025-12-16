import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  InvoiceBasicModel,
  InvoiceModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export abstract class InvoiceApi {
  abstract getInvoices(
    pageable: PageableModel
  ): Observable<ApiResponseModel<SearchResultPayloadModel<InvoiceBasicModel>>>;

  abstract getInvoice(id: number): Observable<ApiResponseModel<InvoiceModel>>;

  abstract getInvoiceByAccountSubscriptionId(accountSubscriptionId: number): Observable<ApiResponseModel<InvoiceModel>>;

  abstract retryInvoicePayment(id: number): Observable<ApiResponseModel<ApiMessage>>;
}
