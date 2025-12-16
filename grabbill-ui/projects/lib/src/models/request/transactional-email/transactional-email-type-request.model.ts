import { BaseIndexFieldModel } from '../../data';
import { WhatsAppTemplateParamModel } from "../../data/whatsapp/whatsapp-template-param.model";

export interface TransactionalEmailTypeRequestModel {
  name: string;
  code: string | null;
  csvSeparator: string;

  emailFrom: string;
  emailFromName: string;
  emailSubject: string;
  emailContent: string;
  emailMjmlContent: string;

  sendSms: boolean;
  smsContent?: string;

  passwordProtected: boolean;
  hasAttachment: boolean;

  archive: boolean;
  autoPurge: boolean;
  autoPurgeByDays: number;

  whatsAppTemplateName: string | null;

  indexFields: BaseIndexFieldModel[];
  whatsAppTemplateParams: WhatsAppTemplateParamModel[];
}
