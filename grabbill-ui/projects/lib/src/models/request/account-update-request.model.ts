export interface AccountUpdateRequestModel {
  companyName: string;
  companyContactNo: string;
  addrLine1?: string;
  addrLine2?: string;
  city?: string;
  state?: string;
  postcode?: string;
  country: string;
  countryIsoCode: string;
  affiliateCode?: string;
}
