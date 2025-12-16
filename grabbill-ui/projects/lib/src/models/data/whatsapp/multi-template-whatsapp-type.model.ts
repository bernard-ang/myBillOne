import { BaseTypeModel } from '../base-type.model';
import { MultiTemplateWhatsappTemplateModel } from './multi-template-whatsapp-template.model';

export interface MultiTemplateWhatsappTypeModel extends BaseTypeModel {
  passwordProtected: boolean;
  hasAttachment: boolean;
  csvSeparator: string;

  total: number;
  statusSent: number;
  statusSkip: number;
  statusDelivered: number;
  statusRead: number;
  statusAcknowledge: number;
  statusFailed: number;

  whatsAppTemplates: MultiTemplateWhatsappTemplateModel[];
}
