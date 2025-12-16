import { BaseRecordModel } from '../base-record.model';

export interface EmailCampaignRecordModel extends BaseRecordModel {
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
}
