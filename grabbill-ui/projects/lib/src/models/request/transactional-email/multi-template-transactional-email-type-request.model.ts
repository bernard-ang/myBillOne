import { BaseIndexFieldModel, MultiTemplateTransactionalEmailTemplateModel } from "../../data";
import { WhatsAppTemplateParamModel } from "../../data/whatsapp/whatsapp-template-param.model";

export interface MultiTemplateTransactionalEmailTypeRequestModel {
  name: string;
  code: string | null;
  csvSeparator: string;

  emailFrom: string;
  emailFromName: string;

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

  transactionalEmailTemplates: MultiTemplateTransactionalEmailTemplateModel[];
}
