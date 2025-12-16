import { BaseRecordModel } from '../base-record.model';

export interface TransactionalEmailRecordModel extends BaseRecordModel {
  emailContent: string;
  emailStatusSent: boolean;
  emailStatusSkipUnsubscribed: boolean;
  emailStatusSkipBounced: boolean;
  emailStatusSoftBounce: boolean;
  emailStatusHardBounce: boolean;
  emailDsnReceivedConfirmation: boolean;
  emailDsnMessage: string;
  dsnProcessedTimestamp: Date;
  emailStatusUserReadTimestamp: Date;
  processedTimestamp: Date;

  emailStatusUnsubscribedTimestamp: Date;
  emailStatusUnsubscribedReason: string;

  whatsappMessageId: string;
  whatsappBodyContent: string;
  whatsappStatusSent: boolean;
  whatsappStatusSentTimestamp: Date;
  whatsappStatusSkip: boolean;
  whatsappStatusSkipReason: string;
  whatsappStatusDelivered: boolean;
  whatsappStatusDeliveredTimestamp: Date;
  whatsappStatusRead: boolean;
  whatsappStatusReadTimestamp: Date;
  whatsappStatusAcknowledge: boolean;
  whatsappStatusAcknowledgeTimestamp: Date
  whatsappStatusFailed: boolean;
  whatsappStatusFailedTimestamp: Date
  whatsappStatusFailedMessage: string;
}
