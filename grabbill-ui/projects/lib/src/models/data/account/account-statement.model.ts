export interface AccountStatementModel {
  id: number;
  startDate: Date;
  endDate: Date
  totalStorageUsed: number;
  maxStorageSize: number;
  totalTransactionalEmailSent: number;
  maxTransactionalEmailSent: number;
  totalEmailCampaignSent: number;
  maxEmailCampaignSent: number;
  totalWhatsappMessageSent: number;
  totalSmsSent: number;
  smsCreditBalance: number;
  smsCreditUsed: number;
  storagePrice: number;
  transactionalEmailPrice: number;
  emailCampaignPrice: number;
  totalPrice: number;
  createdDate: Date;
}
