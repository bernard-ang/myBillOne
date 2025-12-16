import { BaseActivityModel } from '../base-activity.model';
import { WhatsAppRecordModel } from './whatsapp-record.model';
import { MultiTemplateWhatsappActivityTemplateModel } from './multi-template-whatsapp-activity-template.model';

export interface MultiTemplateWhatsappActivityModel extends BaseActivityModel {
  scheduledTimestamp?: Date;

  whatsAppActivityTemplates: MultiTemplateWhatsappActivityTemplateModel[];

  whatsAppStatusSent: number;
  whatsAppStatusSkip: number;
  whatsAppStatusRead: number;
  whatsAppStatusDelivered: number;
  whatsAppStatusAcknowledge: number;
  whatsAppStatusFailed: number;

  records: WhatsAppRecordModel[];
}
