import { InvoiceStatus, PromoCodeBasicModel, SubscriptionMode } from "../data";

export interface PrePlanSwitchInvoicePayloadModel {
  planId: string;
  planName: string;
  planDescription: string;
  storageSize: number;
  storagePrice: number;
  transactionalEmailSize: number;
  transactionalEmailPrice: number;
  emailCampaignSize: number;
  emailCampaignPrice: number;
  cycleStartDate: Date;
  cycleEndDate: Date;
  totalAmount: number;
  discountAmount: number;
  totalTaxPercentage: number;
  totalAmountBeforeDiscount: number;
  totalTaxAmount: number;
  totalAmountWithTax: number;
  mode: SubscriptionMode;
  status: InvoiceStatus;

  promoCode: PromoCodeBasicModel;

  offsetAmount: number;
  oldAmount: number;
  remainingDays: number;
  prevCycleStartDate: Date;
  prevCycleEndDate: Date;

  paymentMethodAvailable: boolean;
  billingAddressAvailable: boolean;
}
