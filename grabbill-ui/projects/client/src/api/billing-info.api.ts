import { Observable } from "rxjs";
import { ApiResponseModel, BillingInfoModel, BillingInfoRequestModel, SearchResultPayloadModel } from "@grabbill/lib";

export abstract class BillingInfoApi {
  abstract getBillingInfos(): Observable<SearchResultPayloadModel<BillingInfoModel>>;

  abstract getBillingInfo(id: number): Observable<ApiResponseModel<BillingInfoModel>>;

  abstract newBillingInfo(request: BillingInfoRequestModel): Observable<ApiResponseModel<BillingInfoModel>>;

  abstract updateBillingInfo(id: number, request: BillingInfoRequestModel): Observable<ApiResponseModel<BillingInfoModel>>;
}
