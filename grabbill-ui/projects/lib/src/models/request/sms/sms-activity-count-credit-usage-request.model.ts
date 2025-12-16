import { BaseIndexRowModel } from '../../data';

export interface SmsActivityCountCreditUsageRequestModel {
  smsContent: string;
  contactGroupId?: number;
  indexRows?: BaseIndexRowModel[];
}
