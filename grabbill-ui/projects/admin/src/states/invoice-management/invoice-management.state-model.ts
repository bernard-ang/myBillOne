import { DownloadFile, InvoiceBasicModel, InvoiceModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface InvoiceManagementStateModel {
  accountName: string;
  invoiceNo: string;
  planName: string;
  status: string;
  startDate?: Date;
  endDate?: Date;

  invoicePageable: PageableModel;
  invoiceSearchResult: SearchResultPayloadModel<InvoiceBasicModel>;
  invoice?: InvoiceModel;

  file?: DownloadFile;
}
