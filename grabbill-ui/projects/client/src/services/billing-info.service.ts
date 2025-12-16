import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { ApiResponseModel, BillingInfoModel, BillingInfoRequestModel, SearchResultPayloadModel } from "@grabbill/lib";
import { ApiHttpService } from "./api-http.service";
import { BillingInfoApi } from "../api/billing-info.api";

@Injectable({
  providedIn: 'root',
})
export class BillingInfoService implements BillingInfoApi {
  readonly baseRoute = 'billing-infos';

  constructor(private http: ApiHttpService) {}

  getBillingInfos (): Observable<SearchResultPayloadModel<BillingInfoModel>> {
    return this.http.get<SearchResultPayloadModel<BillingInfoModel>>(this.baseRoute);
  }

  getBillingInfo (id: number): Observable<ApiResponseModel<BillingInfoModel>> {
    return this.http.get<ApiResponseModel<BillingInfoModel>>(`${this.baseRoute}/${id}`);
  }

  newBillingInfo (request: BillingInfoRequestModel): Observable<ApiResponseModel<BillingInfoModel>> {
    return this.http.post<ApiResponseModel<BillingInfoModel>>(this.baseRoute, request);
  }

  updateBillingInfo (id: number, request: BillingInfoRequestModel): Observable<ApiResponseModel<BillingInfoModel>> {
    return this.http.put<ApiResponseModel<BillingInfoModel>>(`${this.baseRoute}/${id}`, request);
  }
}
