import { BaseActivityModel } from '../base-activity.model';
import { EmailCampaignRecordModel } from './email-campaign-record.model';
import { EmbeddedLinkModel } from '../embedded-link.model';

export interface EmailCampaignActivityModel extends BaseActivityModel {
  scheduledTimestamp?: Date;

  emailFrom: string;
  emailFromName: string;
  emailSubject: string;
  emailContent: string;
  emailMjmlContent: string;

  emailTotal: number;
  emailStatusBounced: number;
  emailStatusBouncedSkip: number;
  emailStatusOpened: number;
  emailStatusSent: number;
  emailStatusUnsubscribed: number;
  emailStatusUnsubscribedSkip: number;

  contactGroupId?: string;
  contactGroupName?: string;

  records: EmailCampaignRecordModel[];

  embeddedLinks: EmbeddedLinkModel[];
}
