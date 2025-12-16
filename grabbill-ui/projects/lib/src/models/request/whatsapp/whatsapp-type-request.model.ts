import { BaseIndexFieldModel, WhatsAppTemplateParamModel } from "../../data";

export interface WhatsAppTypeRequestModel {
  name: string;
  code: string | null;
  csvSeparator: string;

  passwordProtected: boolean;
  hasAttachment: boolean;

  whatsAppTemplateName: string | null;

  indexFields: BaseIndexFieldModel[];
  whatsAppTemplateParams: WhatsAppTemplateParamModel[];
}
