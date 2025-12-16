import { PlanOptionModel } from './plan-option.model';

export interface PlanModel {
  id: number;
  name: string;
  description: string;

  storageOptions: PlanOptionModel[];
  transactionalEmailOptions: PlanOptionModel[];
  emailCampaignOptions: PlanOptionModel[];

  maxAttachmentSize: number;
  grabbillLogo: boolean;
  customSmtp: boolean;
  maxUser: number;
  supportDays: number;
  reporting: boolean;
  scheduleEmail: boolean;
  exportFile: boolean;
}
