import { WhatsappTemplateComponentModel } from "./whatsapp-template-component.model";
import { WhatsappTemplateStatus } from "./whatsapp-template-status";
import { WhatsappCategory } from "./whatsapp-category";

export interface WhatsappTemplateModel {
  id: string;
  name: string;
  language: string;
  status: WhatsappTemplateStatus;
  category: WhatsappCategory;
  waTemplateId: string;
  whatsappId: string;
  component: WhatsappTemplateComponentModel[];
  noOfParams: number;
  isActive: boolean;
  updatedBy: string;
  updatedDate: Date;
}
