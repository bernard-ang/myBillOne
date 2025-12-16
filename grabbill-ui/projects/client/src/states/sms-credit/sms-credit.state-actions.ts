import { SmsCreditTopupRequestModel } from '@grabbill/lib';

export class TopupSmsCredit {
  static readonly type = '[Sms Credits] Topup';

  constructor(public request: SmsCreditTopupRequestModel) {}
}

export class GetSmsPlanOptions {
  static readonly type = '[Sms Credits] GetSmsPlanOptions';

  constructor() {}
}
