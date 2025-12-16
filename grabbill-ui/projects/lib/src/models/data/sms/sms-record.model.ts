import { BaseRecordModel } from '../base-record.model';

export interface SmsRecordModel extends BaseRecordModel {
  smsContent: string;
  smsStatusSent: boolean;
  processedTimestamp: Date;
  creditUsed: number;
  smsStatusCode: number;
  smsErrorMessage: string;
  message: string;
}
