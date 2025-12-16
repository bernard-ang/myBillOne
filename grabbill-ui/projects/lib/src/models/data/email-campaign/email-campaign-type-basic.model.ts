import { BaseTypeBasicModel } from '../base-type-basic.model';

export interface EmailCampaignTypeBasicModel extends BaseTypeBasicModel {
  hasAttachment: boolean;
  lastSentBy: string;
  lastSentDate: Date;
}
