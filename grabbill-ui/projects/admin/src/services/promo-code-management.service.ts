import { Injectable } from '@angular/core';
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
import { ApiHttpService } from './api-http.service';
import { PromoCodeManagementApi } from '../api/promo-code-management.api';

@Injectable({
  providedIn: 'root',
})
export class PromoCodeManagementService implements PromoCodeManagementApi {
  readonly baseRoute = `mgmt/promo-codes`;

  constructor(private http: ApiHttpService) {}

  searchPromoCodes(
    pageable: PageableModel,
    code?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<PromoCodeBasicModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<PromoCodeBasicModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        code,
      }
    );
  }

  getPromoCodeDetails(id: number): Observable<ApiResponseModel<PromoCodeDetailsModel>> {
    return this.http.get<ApiResponseModel<PromoCodeDetailsModel>>(`${this.baseRoute}/${id}`);
  }

  newPromoCode(req: PromoCodeNewRequestModel): Observable<ApiResponseModel<PromoCodeDetailsModel>> {
    return this.http.post<ApiResponseModel<PromoCodeDetailsModel>>(`${this.baseRoute}`, req);
  }

  updatePromoCode(id: number, req: PromoCodeUpdateRequestModel): Observable<ApiResponseModel<PromoCodeDetailsModel>> {
    return this.http.put<ApiResponseModel<PromoCodeDetailsModel>>(`${this.baseRoute}/${id}`, req);
  }

  setPromoCodeStatus(
    id: number,
    req: PromoCodeStatusUpdateRequestModel
  ): Observable<ApiResponseModel<PromoCodeDetailsModel>> {
    return this.http.put<ApiResponseModel<PromoCodeDetailsModel>>(`${this.baseRoute}/${id}/status`, req);
  }
  deletePromoCode(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }
}
