import { TwoFactorAuthType } from './two-factor-auth-type';

export interface TwoFactorAuthorizationRequestModel {
  authType: TwoFactorAuthType;
  email: string;
  otp: number;
}
