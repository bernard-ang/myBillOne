import { PageableModel, PromoCodeBasicModel, PromoCodeDetailsModel, SearchResultPayloadModel } from '@grabbill/lib';

export interface PromoCodeStateModel {
  code?: string;
  promoCodePageable: PageableModel;
  promoCodeSearchResult: SearchResultPayloadModel<PromoCodeBasicModel>;
  promoCode?: PromoCodeDetailsModel;
}
