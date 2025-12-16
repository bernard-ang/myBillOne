import { AuditableModel } from './common';
import { InvoiceStatus } from './invoice-status';
import { InvoiceType } from "./invoice-type";

export interface InvoiceBasicModel extends AuditableModel {
  id: number;
  invoiceNo: string;
  planName: string;
  storageSize: number;
  transactionalEmailSize: number;
  emailCampaignSize: number;
  smsTopupSize: number;
  smsTopupPrice: number;
  cycleStartDate: Date;
  cycleEndDate: Date;
  totalAmountWithTax: number;
  billToName: string;
  billToContactNo: string;
  billToEmail: string;
  status: InvoiceStatus;
  type: InvoiceType;

  accountId: number;
  accountName: string;
}
