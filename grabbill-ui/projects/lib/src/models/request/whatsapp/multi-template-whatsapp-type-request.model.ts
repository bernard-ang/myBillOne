import { BaseIndexFieldModel, MultiTemplateWhatsappTemplateModel } from "../../data";

export interface MultiTemplateWhatsappTypeRequestModel {
  name: string;
  code: string | null;
  csvSeparator: string;

  passwordProtected: boolean;
  hasAttachment: boolean;

  indexFields: BaseIndexFieldModel[];
  whatsAppTemplates: MultiTemplateWhatsappTemplateModel[];
}
