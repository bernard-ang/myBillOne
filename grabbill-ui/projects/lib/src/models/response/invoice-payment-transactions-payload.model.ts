import { InvoiceModel, PaymentTransactionModel } from '../data';

export interface InvoicePaymentTransactionsPayloadModel extends InvoiceModel {
  accountId: number;
  accountName: string;

  paymentTransactions: PaymentTransactionModel[];
}
