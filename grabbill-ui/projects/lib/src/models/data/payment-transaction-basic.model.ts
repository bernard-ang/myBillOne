import { PaymentType } from './payment-type';
import { PaymentStatus } from './payment-status';

export interface PaymentTransactionBasicModel {
  id: number;
  paymentType: PaymentType;
  transactionReferenceNo: PaymentType;
  amount: string;
  currency: string;
  prodDesc: string;
  paymentStatus: PaymentStatus;
  invoiceNo: string;

  accountId: number;
  accountName: string;

  createdBy: string;
  createdDate: Date;
}
