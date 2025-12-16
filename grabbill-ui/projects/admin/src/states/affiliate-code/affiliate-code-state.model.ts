import { AffiliateCodeBasicModel, PageableModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface AffiliateCodeStateModel {
  code?: string;
  newMasterCode?: string;
  affiliateCodePageable: PageableModel;
  affiliateCodeSearchResult: SearchResultPayloadModel<AffiliateCodeBasicModel>;
}
