import { BaseIndexFieldModel, SmsFieldType } from '../../data';

export interface SmsTypeRequestModel {
  name: string;
  code: string;

  smsFrom: string;
  smsContent: string;
  smsFieldType: SmsFieldType;

  contactGroupId?: number;

  indexFields: BaseIndexFieldModel[];
}
