export interface WhatsappTemplateRequestModel {
  name: string;
  category: string;
  hasAttachment: boolean;
  body: string;
  footer: string;
  acknowledgementButton: boolean;
}
