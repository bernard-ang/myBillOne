export interface AccountBasicModel {
  id: number;
  stripeCustomerId: number;
  companyName: string;
  companyContactNo: string;
  plan: string;
  email: string;
  active: boolean;
  paymentExempted: boolean;
  createdTime: Date;
  lastModifiedTime: Date;
}
