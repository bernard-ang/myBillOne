import { InvoiceBasicModel, InvoiceModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface InvoiceStateModel {
  invoicePageable: PageableModel;
  invoiceSearchResult: SearchResultPayloadModel<InvoiceBasicModel>;
  invoice?: InvoiceModel;
}
