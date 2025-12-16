import { Injectable } from '@angular/core';
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
import { ApiHttpService } from './api-http.service';
import { AffiliateCodeManagementApi } from '../api/affiliate-code-management.api';

@Injectable({
  providedIn: 'root',
})
export class AffiliateCodeManagementService implements AffiliateCodeManagementApi {
  readonly baseRoute = `mgmt/affiliate-codes`;

  constructor(private http: ApiHttpService) {}

  getAffiliateCodes(
    pageable: PageableModel,
    code?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AffiliateCodeBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<AffiliateCodeBasicModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        code,
      }
    );
  }

  getAffiliateCode(id: number): Observable<ApiResponseModel<AffiliateCodeDetailsModel>> {
    return this.http.get<ApiResponseModel<AffiliateCodeDetailsModel>>(`${this.baseRoute}/${id}`);
  }

  newAffiliateCode(req: AffiliateCodeRequestModel): Observable<ApiResponseModel<AffiliateCodeDetailsModel>> {
    return this.http.post<ApiResponseModel<AffiliateCodeDetailsModel>>(`${this.baseRoute}`, req);
  }

  updateAffiliateCode(
    id: number,
    req: AffiliateCodeRequestModel
  ): Observable<ApiResponseModel<AffiliateCodeDetailsModel>> {
    return this.http.put<ApiResponseModel<AffiliateCodeDetailsModel>>(`${this.baseRoute}/${id}`, req);
  }

  deleteAffiliateCode(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }

  generateMasterCode(): Observable<ApiResponseModel<AffiliateCodeUniqueMasterCodePayloadModel>> {
    return this.http.get<ApiResponseModel<AffiliateCodeDetailsModel>>(`${this.baseRoute}/generate-master-code`);
  }
}
