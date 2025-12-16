import { BaseTypeBasicModel } from '../base-type-basic.model';

export interface TransactionalEmailTypeBasicModel extends BaseTypeBasicModel {
  hasAttachment: boolean;
  archive: boolean;
  passwordProtected: boolean;
  autoPurge: boolean;
  lastSentBy: string;
  lastSentDate: Date;
}
