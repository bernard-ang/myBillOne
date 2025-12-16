import { PageableModel } from '@grabbill/lib';

export class ResetInvoices {
  static readonly type = '[Invoice] ResetInvoices';
}

export class QueryInvoices {
  static readonly type = '[Invoice] QueryInvoices';

  constructor(public pageable?: PageableModel) {}
}

export class ResetInvoice {
  static readonly type = '[Invoice] ResetInvoice';
}

export class GetInvoice {
  static readonly type = '[Invoice] GetInvoice';

  constructor(public id: number) {}
}

export class GetInvoiceBySubscriptionId {
  static readonly type = '[Invoice] GetInvoiceBySubscriptionId';

  constructor(public accountSubscriptionId: number) {}
}

export class PayInvoice {
  static readonly type = '[Invoice] PayInvoice';

  constructor(public id: number) {}
}
