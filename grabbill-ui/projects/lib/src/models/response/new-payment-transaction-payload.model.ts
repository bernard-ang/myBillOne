import { AccountSubscriptionModel } from "../data";

export interface NewPaymentTransactionPayloadModel {
  merchantCode: string;
  paymentId: string;
  refNo: string;
  amountStr: string;
  amount: number;
  currency: string;
  prodDesc: string;
  userName: string;
  userEmail: string;
  userContact: string;
  lang: string;
  signatureType: string;
  signature: string;
  remark: string;
  responseUrl: string;
  backendUrl: string;
  actionType: string;

  ipay88OpsgUrl: string;

  accountSubscription: AccountSubscriptionModel;
}
