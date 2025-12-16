import { AccountModel } from "./account.model";
import { SubscriptionMode } from "./subscription-mode";
import { AuditableModel } from "../common";

export interface AccountSubscriptionModel extends AuditableModel {
  id: number;

  mode: SubscriptionMode;

  storageSize: number;
  storagePrice: number;

  transactionalEmailSize: number;
  transactionalEmailPrice: number;

  emailCampaignSize: number;
  emailCampaignPrice: number;

  planName: string;
  planDescription: string;
  maxAttachmentSize: number;
  grabbillLogo: boolean;
  customSmtp: boolean;
  maxUser: number;
  supportDays: number;
  reporting: boolean;
  scheduleEmail: boolean;
  exportFile: boolean;

  startDate: Date;
  endDate?: Date;

  account: AccountModel;
}
