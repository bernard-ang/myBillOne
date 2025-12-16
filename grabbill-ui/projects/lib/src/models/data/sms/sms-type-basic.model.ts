import { BaseTypeBasicModel } from '../base-type-basic.model';

export interface SmsTypeBasicModel extends BaseTypeBasicModel {
  lastSentBy: string;
  lastSentDate: Date;
}
