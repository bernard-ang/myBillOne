export interface DashboardStatisticsModel {
  storageUsed: number;
  storageLimit: number;
  transactionalEmailSent: number;
  transactionalEmailLimit: number;
  emailCampaignSent: number;
  emailCampaignLimit: number;
  smsRemainingCredits: number;
  smsTotalCreditsUsed: number;
  startDateTime: Date;
  endDateTime: Date;
}
