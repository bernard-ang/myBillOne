import { PageableModel } from '@grabbill/lib';

export class ResetInvoices {
  static readonly type = '[Invoice Management] ResetInvoices';
}

export class QueryInvoices {
  static readonly type = '[Invoice Management] QueryInvoices';

  constructor(
    public pageable: PageableModel,
    public accountName?: string,
    public invoiceNo?: string,
    public planName?: string,
    public status?: string,
    public startDate?: Date,
    public endDate?: Date
  ) {}
}

export class ResetInvoice {
  static readonly type = '[Invoice Management] ResetInvoice';
}

export class GetInvoice {
  static readonly type = '[Invoice Management] GetInvoice';

  constructor(public id: number) {}
}

export class DownloadInvoiceReport {
  static readonly type = '[Invoice] DownloadInvoiceReport';

  constructor(
    public startDate?: Date,
    public endDate?: Date,
    public affiliateCode?: string,
    public accountName?: string
  ) {}
}
