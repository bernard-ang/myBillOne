import { BaseTypeBasicModel } from '../base-type-basic.model';

export interface WhatsAppTypeBasicModel extends BaseTypeBasicModel {
  hasAttachment: boolean;
  passwordProtected: boolean;
  lastSentBy: string;
  lastSentDate: Date;
}
