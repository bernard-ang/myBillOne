import { WhatsappComponentType, WhatsappTemplateModel } from '@grabbill/lib';

export const getWhatsAppTemplateBody = (template: WhatsappTemplateModel) => {
  return template.component.find((component) => component.type === WhatsappComponentType.BODY)?.text || '-';
};

export const getWhatsAppTemplateHeader = (template: WhatsappTemplateModel) => {
  return template.component.find((component) => component.type === WhatsappComponentType.HEADER)?.format || '-';
}

export const getWhatsAppTemplateFooter = (template: WhatsappTemplateModel) => {
  return template.component.find((component) => component.type === WhatsappComponentType.FOOTER)?.text || '-';
}

export const getWhatsAppTemplateButton = (template: WhatsappTemplateModel) => {
  const buttonsComponent = template.component.find((component) => component.type === WhatsappComponentType.BUTTONS);
  return buttonsComponent?.buttons.map((button) => button.text).join(',') || '-';
}

export const getWhatsAppTemplateButtonUrl = (template: WhatsappTemplateModel) => {
  const buttonsComponent = template.component.find((component) => component.type === WhatsappComponentType.BUTTONS);
  return buttonsComponent?.buttons.map((button) => button.url).join(',') || '/ack/whatsapp?recordId={{1}}';
}

export const getWhatsAppTemplateBodyParams = (template: WhatsappTemplateModel) => {
  const body = getWhatsAppTemplateBody(template)
  const regex = /\{\{([\d]+)\}\}/g;
  return body.match(regex);
}
