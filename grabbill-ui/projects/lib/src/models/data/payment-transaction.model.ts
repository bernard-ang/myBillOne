import { PaymentType } from './payment-type';
import { PaymentStatus } from './payment-status';

export interface PaymentResponse {
  id: number;
  transId: string;
  amount: number;
  authCode: string;
  status: string;
  errDesc: string;
  signature: string;
  tokenId: string;
  ccName: string;
  ccNo: string;
  bankName: string;
  country: string;
  bindCardErrDesc: string;
  createdBy: string;
  createdDate: Date;
  remark: string;
}

export interface PaymentTransactionModel {
  id: number;
  paymentType: PaymentType;
  merchantCode: string;
  paymentId: string;
  refNo: string;
  amount: string;
  currency: string;
  prodDesc: string;
  userName: string;
  userEmail: string;
  userContact: string;
  lang: string;
  signatureType: string;
  signature: string;
  remark: string;
  actionType: string;
  paymentStatus: PaymentStatus;
  paymentErrorMessage: string;
  createdBy: string;
  createdDate: Date;

  accountId: number;
  accountName: string;

  paymentResponses: PaymentResponse[];
}
