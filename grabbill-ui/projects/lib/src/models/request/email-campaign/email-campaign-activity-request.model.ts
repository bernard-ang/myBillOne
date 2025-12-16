import { ProcessStatus } from '../../data';

export interface EmailCampaignActivityRequestModel {
  scheduledTimestamp?: Date;
  name: string;
  emailFrom: string;
  emailFromName: string;
  emailSubject: string;
  emailContent: string;
  emailMjmlContent: string;
  contactGroupId?: number;
  status: ProcessStatus;
}
