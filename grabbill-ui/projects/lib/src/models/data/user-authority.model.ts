import { AccountModel, AccountSubscriptionModel } from './account';
import { Privilege } from './privilege';
import { BillingInfoModel } from './billing-info.model';

export interface UserAuthorityModel {
  name: string;
  email: string;
  role: string;
  privileges: Privilege[];
  active: boolean;
  verified: boolean;
  account: AccountModel;
  subscription?: AccountSubscriptionModel;
  paymentMethodRequired: boolean;
  paymentGracePeriodExceeded: boolean;
  billingInfo?: BillingInfoModel;
  guidedStepsViewed: boolean;
  outstandingAmount: number;
  google2FAEnabled: boolean;
  email2FAEnabled: boolean;
}
