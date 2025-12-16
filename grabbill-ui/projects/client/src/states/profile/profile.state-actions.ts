import {
  TwoFactorAuthorizationRequestModel,
  TwoFactorAuthUpdateRequestModel,
  UserPasswordChangeRequestModel,
  UserProfileUpdateRequestModel
} from "@grabbill/lib";

export class UpdateProfile {
  static readonly type = '[Profile] Update Profile';

  constructor(public request: UserProfileUpdateRequestModel) {}
}

export class ChangePassword {
  static readonly type = '[Profile] Change Password';

  constructor(public request: UserPasswordChangeRequestModel) {}
}

export class MarkGuidedStepsViewed {
  static readonly type = '[Profile] Mark Guided Steps Viewed';

  constructor() {}
}

export class UpdateTwoFactorAuth {
  static readonly type = '[Profile] Update Two Factor Auth';

  constructor(public request: TwoFactorAuthUpdateRequestModel) {}
}

export class ActivateTwoFactorAuth {
  static readonly type = '[Profile] Activate Two Factor Auth';

  constructor(public request: TwoFactorAuthorizationRequestModel) {}
}

export class GenerateQrcode {
  static readonly type = '[Profile] Generate QRCode';
}
