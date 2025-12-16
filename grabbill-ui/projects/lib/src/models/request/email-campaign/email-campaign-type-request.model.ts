export interface EmailCampaignTypeRequestModel {
  name: string;
  code: string;

  emailFrom: string;
  emailFromName: string;
  emailSubject: string;
  emailContent: string;
  emailMjmlContent: string;

  hasAttachment: boolean;

  autoPurge: boolean;
  autoPurgeByDays: number;

  contactGroupId?: number;
}
