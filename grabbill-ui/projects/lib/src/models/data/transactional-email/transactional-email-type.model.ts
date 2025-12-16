import { BaseTypeModel } from '../base-type.model';
import { WhatsAppTemplateParamModel } from '../whatsapp/whatsapp-template-param.model';

export interface TransactionalEmailTypeModel extends BaseTypeModel {
  emailFrom: string;
  emailFromName: string;
  emailTo: string;
  emailSubject: string;
  emailContent: string;
  emailMjmlContent: string;
  emailAttachmentFileName: string;

  autoPurge: boolean;
  autoPurgeByDays: number;

  sendSms: boolean;
  smsContent: string;

  passwordProtected: boolean;
  hasAttachment: boolean;

  archive: boolean;

  emailTotal: number;
  emailStatusBounced: number;
  emailStatusBouncedSkip: number;
  emailStatusOpened: number;
  emailStatusSent: number;
  emailStatusUnsubscribed: number;
  emailStatusUnsubscribedSkip: number;

  whatsAppTemplateName: string;
  whatsAppTemplateParams: WhatsAppTemplateParamModel[];
}
