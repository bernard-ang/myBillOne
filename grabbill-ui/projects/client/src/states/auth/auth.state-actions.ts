import {
  AccountSubscriptionModel,
  AuthorizationRequestModel,
  EmailOtpRequestModel,
  ForgetPasswordRequestModel,
  LoginRequestModel,
  ResetPasswordRequestModel,
  TwoFactorAuthorizationRequestModel,
} from '@grabbill/lib';

export class Login {
  static readonly type = '[Auth] Login';

  constructor(public request: LoginRequestModel) {}
}

export class Authorize {
  static readonly type = '[Auth] Authorize';

  constructor(public request: AuthorizationRequestModel) {}
}

export class Me {
  static readonly type = '[Auth] Me';
}

export class Logout {
  static readonly type = '[Auth] Logout';
}

export class RefreshToken {
  static readonly type = '[Auth] RefreshToken';
}

export class ForgetPassword {
  static readonly type = '[Auth] ForgetPassword';

  constructor(public request: ForgetPasswordRequestModel) {}
}

export class ResetPassword {
  static readonly type = '[Auth] ResetPassword';

  constructor(public request: ResetPasswordRequestModel) {}
}

export class UpdateSubscription {
  static readonly type = '[Auth] UpdateSubscription';

  constructor(public subscription: AccountSubscriptionModel) {}
}

export class GenerateOtpEmail {
  static readonly type = '[Auth] Generate OTP Email';

  constructor(public request: EmailOtpRequestModel) {}
}

export class GenerateActivateTwoFactorAuthorizationEmail {
  static readonly type = '[Auth] Generate Activate Two Factor Authorization Email';

  constructor(public request: EmailOtpRequestModel) {}
}

export class TwoFactorAuthorization {
  static readonly type = '[Auth] TwoFactorAuthorization';

  constructor(public request: TwoFactorAuthorizationRequestModel) {}
}
