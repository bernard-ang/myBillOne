import { TwoFactorAuthorizationRequestModel, TwoFactorAuthUpdateRequestModel } from "@grabbill/lib";

export class UpdateTwoFactorAuth {
  static readonly type = '[Admin Profile] Update Two Factor Auth';

  constructor(public request: TwoFactorAuthUpdateRequestModel) {}
}

export class GenerateQrcode {
  static readonly type = '[Admin Profile] Generate QRCode';
}

export class ActivateTwoFactorAuth {
  static readonly type = '[Admin Profile] Activate Two Factor Auth';

  constructor(public request: TwoFactorAuthorizationRequestModel) {}
}
