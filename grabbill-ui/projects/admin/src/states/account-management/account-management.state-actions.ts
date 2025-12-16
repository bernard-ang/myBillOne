import { WabaUpdateRequestModel, PageableModel, UserPlanUpdateRequestModel } from "@grabbill/lib";

export class ResetAccounts {
  static readonly type = '[Account Management] ResetAccounts';
}

export class QueryAccounts {
  static readonly type = '[Account Management] QueryAccounts';

  constructor(
    public pageable: PageableModel,
    public filters: { [key: string]: any },
    public name: string | undefined,
    public startDate: Date | undefined,
    public endDate: Date | undefined
  ) {}
}

export class ResetAccount {
  static readonly type = '[Account Management] ResetAccount';
}

export class GetAccount {
  static readonly type = '[Admin Account Management] GetAccount';

  constructor(public id: number) {}
}

export class UpdateAccountStatus {
  static readonly type = '[Admin Account Management] UpdateAccountStatus';

  constructor(public id: number, public active: boolean) {}
}

export class UpdateAccountPaymentExemptionStatus {
  static readonly type = '[Admin Account Management] UpdateAccountPaymentExemptionStatus';

  constructor(public id: number, public paymentExemption: boolean) {}
}

export class SwitchAccountPlan {
  static readonly type = '[Admin Account Management] SwitchAccountPlan';

  constructor(public id: number, public request: UserPlanUpdateRequestModel) {}
}

export class GetAccountUsers {
  static readonly type = '[Admin Account Management] GetAccountUsers';

  constructor(public id: number) {}
}

export class DownloadAccountReport {
  static readonly type = '[Admin Account Management] DownloadAccountReport';

  constructor(public startDate?: Date, public endDate?: Date, public affiliateCode?: string) {}
}

export class UpdateWabaInfo {
  static readonly type = '[Admin Account Management] UpdateWabaInfo';

  constructor(public id: number, public request: WabaUpdateRequestModel) {}
}

export class TestWabaLogin {
  static readonly type = '[Admin Account Management] TestWabaLogin';

  constructor(public id: number) {}
}

export class RegisterWabaWebhook {
  static readonly type = '[Admin Account Management] RegisterWabaWebhook';

  constructor(public id: number) {}
}

export class UnregisterWabaWebhook {
  static readonly type = '[Admin Account Management] UnregisterWabaWebhook';

  constructor(public id: number) {}
}

export class GetAccountWabaTemplates {
  static readonly type = '[Admin Account Management] GetAccountWabaTemplates';

  constructor(public id: number) {}
}
