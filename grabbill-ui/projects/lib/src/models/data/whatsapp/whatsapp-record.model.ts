import { BaseRecordModel } from '../base-record.model';

export interface WhatsAppRecordModel extends BaseRecordModel {
  processedTimestamp: Date;

  whatsAppMessageId: string;
  whatsAppBodyContent: string;
  whatsAppStatusSent: boolean;
  whatsAppStatusSentTimestamp: Date;
  whatsAppStatusSkip: boolean;
  whatsAppStatusSkipReason: string;
  whatsAppStatusDelivered: boolean;
  whatsAppStatusDeliveredTimestamp: Date;
  whatsAppStatusRead: boolean;
  whatsAppStatusReadTimestamp: Date;
  whatsAppStatusAcknowledge: boolean;
  whatsAppStatusAcknowledgeTimestamp: Date
  whatsAppStatusFailed: boolean;
  whatsAppStatusFailedTimestamp: Date
  whatsAppStatusFailedMessage: string;
}
