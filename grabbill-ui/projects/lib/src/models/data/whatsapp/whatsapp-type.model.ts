import { BaseTypeModel } from '../base-type.model';
import { WhatsAppTemplateParamModel } from "./whatsapp-template-param.model";

export interface WhatsAppTypeModel extends BaseTypeModel {
  passwordProtected: boolean;
  hasAttachment: boolean;
  csvSeparator: string;

  total: number;
  statusSent: number;
  statusSkip: number;
  statusDelivered: number;
  statusRead: number;
  statusAcknowledge: number;

  whatsAppTemplateName: string;
  whatsAppTemplateParams: WhatsAppTemplateParamModel[];
}
