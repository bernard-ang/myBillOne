import { BaseTypeModel } from '../base-type.model';
import { MultiTemplateTransactionalEmailTemplateModel } from "./multi-template-transactional-email-template.model";
import { WhatsAppTemplateParamModel } from "../whatsapp";

export interface MultiTemplateTransactionalEmailTypeModel extends BaseTypeModel {
  emailFrom: string;
  emailFromName: string;
  emailTo: string;
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

  transactionalEmailTemplates: MultiTemplateTransactionalEmailTemplateModel[];
}
