import { AuditableModel } from "@grabbill/lib";

export interface BillingInfoModel extends AuditableModel {
  id: number;
  name: string;
  contactNo: string;
  email: string;

  addrLine1: string;
  addrLine2: string;
  city: string;
  state: string;
  postcode: string;
  country: string;
}
