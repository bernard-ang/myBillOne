import { AccountSubscriptionModel } from '@grabbill/lib';

export interface SwitchPlanAccountSubscriptionPayloadModel extends AccountSubscriptionModel {
  paymentMethodAvailable: boolean;
}
