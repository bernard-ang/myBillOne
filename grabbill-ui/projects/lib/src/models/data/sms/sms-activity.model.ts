import { BaseActivityModel } from '../base-activity.model';

export interface SmsActivityModel extends BaseActivityModel {
  scheduledTimestamp?: Date;

  smsFrom: string;
  smsFromName: string;
  smsContent: string;

  contactGroupId?: number;
  contactGroupName?: string;

  totalSms: number;
  smsStatusSent: number;
  smsStatusError: number;
  smsCreditUsed: number;
}
