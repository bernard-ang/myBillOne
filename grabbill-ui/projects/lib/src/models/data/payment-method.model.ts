import { AuditableModel } from "@grabbill/lib";

export interface PaymentMethodModel extends AuditableModel {
  id: number;
  refNo: string;
  bankName: string;
  country: string;
  ccName: string;
  ccNo: string;
}
