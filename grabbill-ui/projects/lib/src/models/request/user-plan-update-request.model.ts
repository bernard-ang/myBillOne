import { SubscriptionMode } from '../data';

export interface UserPlanUpdateRequestModel {
  planId: string;
  storageSize: number;
  transactionalEmailSize: number;
  emailCampaignSize: number;
  subscriptionMode: SubscriptionMode;
  promoCode?: string;
}
