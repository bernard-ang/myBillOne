import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  PageableModel,
  PromoCodeBasicModel,
  PromoCodeDetailsModel,
  PromoCodeNewRequestModel,
  PromoCodeStatusUpdateRequestModel,
  PromoCodeUpdateRequestModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export abstract class PromoCodeManagementApi {
  abstract searchPromoCodes(
    pageable: PageableModel,
    code?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<PromoCodeBasicModel>>>;

  abstract getPromoCodeDetails(id: number): Observable<ApiResponseModel<PromoCodeDetailsModel>>;

  abstract newPromoCode(req: PromoCodeNewRequestModel): Observable<ApiResponseModel<PromoCodeDetailsModel>>;

  abstract updatePromoCode(
    id: number,
    req: PromoCodeUpdateRequestModel
  ): Observable<ApiResponseModel<PromoCodeDetailsModel>>;

  abstract setPromoCodeStatus(
    id: number,
    req: PromoCodeStatusUpdateRequestModel
  ): Observable<ApiResponseModel<PromoCodeDetailsModel>>;

  abstract deletePromoCode(id: number): Observable<ApiResponseModel<ApiMessage>>;
}
