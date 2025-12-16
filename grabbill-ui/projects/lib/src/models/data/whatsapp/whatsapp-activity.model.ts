import { BaseActivityModel } from '../base-activity.model';
import { WhatsAppRecordModel } from "./whatsapp-record.model";

export interface WhatsAppActivityModel extends BaseActivityModel {
  scheduledTimestamp?: Date;

  whatsappTemplateName: string;
  whatsappBodyContent: string;
  whatsappDocument: boolean;
  whatsappFooterContent: string;
  whatsappButton: string;

  whatsAppStatusSent: number;
  whatsAppStatusSkip: number;
  whatsAppStatusRead: number;
  whatsAppStatusDelivered: number;
  whatsAppStatusAcknowledge: number;

  records: WhatsAppRecordModel[];

  sftp?: boolean;
  sftpPath?: string;
}
