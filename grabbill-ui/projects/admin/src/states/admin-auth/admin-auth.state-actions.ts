import {
  EmailOtpRequestModel,
  ForgetPasswordRequestModel,
  LoginRequestModel,
  ResetPasswordRequestModel,
  TwoFactorAuthorizationRequestModel,
} from '@grabbill/lib';

export class Login {
  static readonly type = '[Admin Auth] Login';

  constructor(public request: LoginRequestModel) {}
}

export class Me {
  static readonly type = '[Admin Auth] Me';
}

export class Logout {
  static readonly type = '[Admin Auth] Logout';
}

export class RefreshToken {
  static readonly type = '[Admin Auth] RefreshToken';
}

export class ForgetPassword {
  static readonly type = '[Admin Auth] ForgetPassword';

  constructor(public request: ForgetPasswordRequestModel) {}
}

export class ResetPassword {
  static readonly type = '[Admin Auth] ResetPassword';

  constructor(public request: ResetPasswordRequestModel) {}
}

export class GenerateOtpEmail {
  static readonly type = '[Admin Auth] Generate OTP Email';

  constructor(public request: EmailOtpRequestModel) {}
}

export class GenerateActivateTwoFactorAuthorizationEmail {
  static readonly type = '[Admin Auth] Generate Activate Two Factor Authorization Email';

  constructor(public request: EmailOtpRequestModel) {}
}

export class TwoFactorAuthorization {
  static readonly type = '[Admin Auth] TwoFactorAuthorization';

  constructor(public request: TwoFactorAuthorizationRequestModel) {}
}
