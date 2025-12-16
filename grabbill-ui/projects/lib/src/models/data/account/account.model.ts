export interface AccountModel {
  companyName: string;
  companyContactNo: string;
  addrLine1: string;
  addrLine2: string;
  city: string;
  state: string;
  postcode: string;
  country: string;
  countryIsoCode: string;
  active: boolean;

  paymentExempted: boolean;

  wabaAutoReplyMessage: string;

  wabaEmail?: string;
  wabaId?: string;
  wabaGuid?: string;
  wabaName?: string;
  wabaPhone?: string;
  wabaPhoneId?: string;
  wabaWebhookId?: string;
  wabaWebhookUrl?: string;

  sftpHost?: string;
  sftpPort?: number;
  sftpUsername?: string;
  sftpPassword?: string;
}
