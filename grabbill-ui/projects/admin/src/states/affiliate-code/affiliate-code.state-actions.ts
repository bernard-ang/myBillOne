import { AffiliateCodeRequestModel, PageableModel } from '@grabbill/lib';

export class ResetAffiliateCodes {
  static readonly type = '[Affiliate Code] ResetAffiliateCodes';
}

export class QueryAffiliateCodes {
  static readonly type = '[Affiliate Code] QueryAffiliateCodes';

  constructor(public pageable?: PageableModel, public code?: string) {}
}

export class NewAffiliateCode {
  static readonly type = '[Affiliate Code] NewAffiliateCode';

  constructor(public request: AffiliateCodeRequestModel) {}
}

export class UpdateAffiliateCode {
  static readonly type = '[Affiliate Code] UpdateAffiliateCode';

  constructor(public id: number, public request: AffiliateCodeRequestModel) {}
}

export class DeleteAffiliateCode {
  static readonly type = '[Affiliate Code] DeleteAffiliateCode';

  constructor(public id: number) {}
}

export class GenerateMasterCode {
  static readonly type = '[Affiliate Code] GenerateMasterCode';
}
