import { PaymentStatus } from './payment-status';

export interface ManageCardResponseModel {
  id: number;
  param1: string;
  param2: string;
  param3: string;
  merchantCode: string;
  tokenId: string;
  status: PaymentStatus;
  actionType: string;
  referenceNo: string;
  remark: string;
  ccName: string;
  ccNo: string;
  bankName: string;
  country: string;
  cccOldTokenId: string;
  createdBy: string;
  createdDate: Date;
}
