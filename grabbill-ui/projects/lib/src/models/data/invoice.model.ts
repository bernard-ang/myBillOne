import { AuditableModel } from './common';
import { InvoiceStatus } from './invoice-status';
import { StripeEventModel } from './stripe';
import { InvoiceType } from "./invoice-type";
import { PromoCodeBasicModel } from "./promo-code";
import { AccountSubscriptionModel } from "./account";

export interface InvoiceModel extends AuditableModel {
  id: number;
  invoiceNo: string;

  planName: string;
  planDescription: string;

  storageSize: number;
  storagePrice: number;
  transactionalEmailSize: number;
  transactionalEmailPrice: number;
  emailCampaignSize: number;
  emailCampaignPrice: number;
  smsTopupSize: number;
  smsTopupPrice: number;

  cycleStartDate: Date;
  cycleEndDate: Date;

  totalAmount: number;
  overdueAmount: number;
  totalTaxPercentage: number;
  totalTaxAmount: number;
  totalAmountWithTax: number;
  totalAmountBeforeDiscount: number;
  offsetAmount: number;
  discountAmount: number;

  promoCode: PromoCodeBasicModel;

  billToName: string;
  billToContactNo: string;
  billToEmail: string;
  billToAddrLine1: string;
  billToAddrLine2: string;
  billToCity: string;
  billToState: string;
  billToPostcode: string;
  billToCountry: string;

  status: InvoiceStatus;
  type: InvoiceType;

  events: StripeEventModel[];

  accountSubscription: AccountSubscriptionModel;
}
