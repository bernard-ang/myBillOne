import {
  AccountUpdateRequestModel,
  NewUserAccountEmailVerificationRequestModel,
  NewUserRegistrationRequestModel, PageableModel,
  ResendVerifyEmailRequestModel, UserAccountUpdateSftpSettingsRequestModel,
  UserPlanUpdateRequestModel, WabaUpdateRequestModel
} from "@grabbill/lib";

export class Register {
  static readonly type = '[Account] Register';

  constructor(public request: NewUserRegistrationRequestModel) {}
}

export class UpdateAccount {
  static readonly type = '[Account] Update Account';

  constructor(public request: AccountUpdateRequestModel) {}
}

export class UpdatePlan {
  static readonly type = '[Account] Update Plan';

  constructor(public request: UserPlanUpdateRequestModel) {}
}

export class PreviewPlanSwitch {
  static readonly type = '[Account] Pre Switch Plan';

  constructor(public request: UserPlanUpdateRequestModel) {}
}

export class SwitchPlan {
  static readonly type = '[Account] Switch Plan';
}

export class ResendVerifyEmail {
  static readonly type = '[User] Resend Verify Email';

  constructor(public request: ResendVerifyEmailRequestModel) {}
}

export class VerifyEmail {
  static readonly type = '[Account] Verify Email';

  constructor(public request: NewUserAccountEmailVerificationRequestModel) {}
}

export class ResetAccountSubscriptions {
  static readonly type = '[Account] Reset Account Subscriptions';
}

export class QueryAccountSubscriptions {
  static readonly type = '[Account] Query Account Subscriptions';

  constructor(public pageable?: PageableModel) {}
}


export class UpdateWabaInfo {
  static readonly type = '[Account] UpdateWabaInfo';

  constructor(public request: WabaUpdateRequestModel) {}
}

export class TestWabaLogin {
  static readonly type = '[Account] TestWabaLogin';

  constructor() {}
}

export class RegisterWabaWebhook {
  static readonly type = '[Account] RegisterWabaWebhook';

  constructor() {}
}

export class UnregisterWabaWebhook {
  static readonly type = '[Account] UnregisterWabaWebhook';

  constructor() {}
}


export class UpdateSftp {
  static readonly type = '[Account] UpdateSftp';

  constructor(public request: UserAccountUpdateSftpSettingsRequestModel) {}
}
