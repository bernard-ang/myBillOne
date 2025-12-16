import { AuditableModel } from "@grabbill/lib";

export interface BillingInfoRequestModel extends AuditableModel {
  name: string;
  contactNo: string;
  email: string;
  addrLine1: string;
  addrLine2: string;
  city: string;
  state: string;
  postcode: string;
  country: string;
  countryIsoCode: string;
  defaultBillInfo: boolean;
}
