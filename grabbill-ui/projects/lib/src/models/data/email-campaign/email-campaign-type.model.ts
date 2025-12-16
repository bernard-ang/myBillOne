import { BaseTypeModel } from '../base-type.model';

export interface EmailCampaignTypeModel extends BaseTypeModel {
  emailFrom: string;
  emailFromName: string;
  emailSubject: string;
  emailContent: string;
  emailMjmlContent: string;
  emailAttachmentFileName: string;

  autoPurge: boolean;
  autoPurgeByDays: number;

  hasAttachment: boolean;

  contactGroupId?: number;
  contactGroupName?: string;

  emailTotal: number;
  emailStatusSent: number;
  emailStatusUnsubscribed: number;
  emailStatusUnsubscribedSkip: number;
  emailStatusBounced: number;
  emailStatusBouncedSkip: number;
  emailStatusOpened: number;
}
