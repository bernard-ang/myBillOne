import { PaymentStatus } from './payment-status';

export interface ManageCardTransactionBasicModel {
  id: number;
  tokenId: string;
  newTokenId: string;
  referenceNo: string;
  remark: string;
  transactionStatus: PaymentStatus;

  createdDate: Date;
  createdBy: string;

  accountId: number;
  accountName: string;
}
