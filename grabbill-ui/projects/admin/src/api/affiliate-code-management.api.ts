import { Observable } from 'rxjs';
import {
  AffiliateCodeBasicModel,
  AffiliateCodeDetailsModel,
  AffiliateCodeRequestModel,
  AffiliateCodeUniqueMasterCodePayloadModel,
  ApiMessage,
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export abstract class AffiliateCodeManagementApi {
  abstract getAffiliateCodes(
    pageable: PageableModel,
    code?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AffiliateCodeBasicModel>>>;

  abstract getAffiliateCode(id: number): Observable<ApiResponseModel<AffiliateCodeDetailsModel>>;

  abstract newAffiliateCode(req: AffiliateCodeRequestModel): Observable<ApiResponseModel<AffiliateCodeDetailsModel>>;

  abstract updateAffiliateCode(
    id: number,
    req: AffiliateCodeRequestModel
  ): Observable<ApiResponseModel<AffiliateCodeDetailsModel>>;

  abstract deleteAffiliateCode(id: number): Observable<ApiResponseModel<ApiMessage>>;
  abstract generateMasterCode(): Observable<ApiResponseModel<AffiliateCodeUniqueMasterCodePayloadModel>>;
}
