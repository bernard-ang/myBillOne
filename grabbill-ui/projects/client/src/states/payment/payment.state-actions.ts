import { StripeSessionType, UserPlanUpdateRequestModel } from '@grabbill/lib';

export class ResetPayment {
  static readonly type = '[Payment] Reset';
}

export class SetupStripeSession {
  static readonly type = '[Payment] Setup Stripe Session';

  constructor(public sessionType: StripeSessionType, public request?: UserPlanUpdateRequestModel) {}
}
