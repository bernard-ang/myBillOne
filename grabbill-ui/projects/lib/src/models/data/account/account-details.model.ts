export interface AccountDetailsModel {
  id: number;
  stripeCustomerId: number;
  companyName: string;
  companyContactNo: string;
  plan: string;

  affiliateCode: string;
  address: string;
  createdTime: Date;
  active: boolean;
  maxUser?: number;

  storageUsed: number;
  storageLimit: number;
  transactionalEmailSent: number;
  transactionalEmailLimit: number;
  emailCampaignSent: number;
  emailCampaignLimit: number;
  totalWhatsappMessageSent: number;
  totalSmsSent: number;
  smsCredit: number;
  smsCreditUsed: number;

  billingName?: string;
  billingAddress?: string;

  paymentExempted: boolean;

  wabaEmail?: string;
  wabaId?: string;
  wabaGuid?: string;
  wabaName?: string;
  wabaPhone?: string;
  wabaPhoneId?: string;
  wabaWebhookId?: string;
  wabaWebhookUrl?: string;
}
