import { WhatsappButtonModel, WhatsappComponentType } from ".";

export interface WhatsappTemplateComponentModel {
  type: WhatsappComponentType;
  format: string;
  text: string;
  buttons: WhatsappButtonModel[];
}
