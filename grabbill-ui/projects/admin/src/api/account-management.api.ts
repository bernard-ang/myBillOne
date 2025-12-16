import { Observable } from 'rxjs';
import {
  AccountBasicModel,
  AccountDetailsModel,
  AdminAccountPaymentExemptionUpdateRequestModel, WabaUpdateRequestModel, ApiMessage,
  ApiResponseModel,
  DownloadFile,
  PageableModel,
  SearchResultPayloadModel,
  UserModel,
  UserPlanUpdateRequestModel,
  WhatsappTemplateModel
} from "@grabbill/lib";

export abstract class AccountManagementApi {
  abstract getAccounts(
    pageable: PageableModel,
    name?: string,
    filters?: { [index: string]: any },
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AccountBasicModel>>>;

  abstract getAccount(id: number): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract getAccountUsers(id: number): Observable<ApiResponseModel<{ users: UserModel[] }>>;

  abstract updateAccountStatus(id: number, req: { active: boolean }): Observable<ApiResponseModel<AccountDetailsModel>>;
  abstract updatePaymentExemptionStatus(
    id: number,
    req: AdminAccountPaymentExemptionUpdateRequestModel
  ): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract switchPlan(id: number, req: UserPlanUpdateRequestModel): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract downloadReport(startDate?: Date, endDate?: Date, affiliateCode?: string): Observable<DownloadFile>;

  abstract updateWaba(id: number, req: WabaUpdateRequestModel): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract wabaLogin(id: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract registerWabaWebhook(id: number): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract unregisterWabaWebhook(id: number): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract getAccountWabaTemplates(id: number): Observable<ApiResponseModel<{ templates: WhatsappTemplateModel[] }>>;
}
