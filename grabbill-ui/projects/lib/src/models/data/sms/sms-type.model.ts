import { BaseTypeModel } from '../base-type.model';
import { SmsFieldType } from './sms-field-type';

export interface SmsTypeModel extends BaseTypeModel {
  smsFrom: string;
  smsContent: string;
  smsFieldType: SmsFieldType;
  totalSms: number;
  smsStatusSent: number;
  smsStatusError: number;
  totalCreditUsed: number;
  totalActivities: number;
  contactGroupId?: number;
  contactGroupName?: string;
}
