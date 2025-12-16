import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AccountBasicModel,
  AccountDetailsModel,
  AdminAccountPaymentExemptionUpdateRequestModel,
  WabaUpdateRequestModel,
  ApiMessage,
  ApiResponseModel,
  DownloadFile,
  PageableModel,
  SearchResultPayloadModel,
  UserModel,
  UserPlanUpdateRequestModel,
  WhatsappTemplateModel
} from "@grabbill/lib";
import { ApiHttpService } from './api-http.service';
import { AccountManagementApi } from '../api/account-management.api';

@Injectable({
  providedIn: 'root',
})
export class AccountManagementService implements AccountManagementApi {
  readonly baseRoute = `mgmt/accounts`;

  constructor(private http: ApiHttpService) {}

  getAccounts(
    pageable: PageableModel,
    name?: string,
    filters?: { [index: string]: any },
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AccountBasicModel>>> {
    startDate?.setHours(0, 0, 0, 0);
    endDate?.setHours(23, 59, 59, 9999);

    return this.http.query<ApiResponseModel<SearchResultPayloadModel<AccountBasicModel>>>(
      `${this.baseRoute}`,
      pageable,
      {
        ...filters,
        name,
        startDate,
        endDate
      }
    );
  }

  getAccount(id: number): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.get<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/${id}`);
  }

  getAccountUsers(id: number): Observable<ApiResponseModel<{ users: UserModel[] }>> {
    return this.http.get<ApiResponseModel<{ users: UserModel[] }>>(`${this.baseRoute}/${id}/users`);
  }

  updateAccountStatus(id: number, req: { active: boolean }): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.put<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/${id}/status`, req);
  }

  switchPlan(id: number, req: UserPlanUpdateRequestModel): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.put<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/${id}/plan`, req);
  }

  updatePaymentExemptionStatus(
    id: number,
    req: AdminAccountPaymentExemptionUpdateRequestModel
  ): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.put<ApiResponseModel<AccountDetailsModel>>(
      `${this.baseRoute}/${id}/payment-exemption-status`,
      req
    );
  }

  downloadReport(startDate?: Date, endDate?: Date, affiliateCode?: string): Observable<DownloadFile> {
    let query = `${this.baseRoute}/reports?tz=${Intl.DateTimeFormat().resolvedOptions().timeZone}`;

    if (startDate) {
      query += `&startDate=${startDate?.toISOString()}`;
    }

    if (endDate) {
      query += `&endDate=${endDate?.toISOString()}`;
    }

    if (affiliateCode) {
      query += `&affiliateCode=${affiliateCode}`;
    }

    return this.http.getBlob(query);
  }

  updateWaba (id: number, req: WabaUpdateRequestModel): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.put<ApiResponseModel<AccountDetailsModel>>(
      `${this.baseRoute}/${id}/waba`,
      req
    );
  }

  wabaLogin (id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.get<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}/waba/login`);
  }

  registerWabaWebhook (id: number): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.get<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/${id}/waba/register-webhook`);
  }

  unregisterWabaWebhook (id: number): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.get<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/${id}/waba/unregister-webhook`);
  }

  getAccountWabaTemplates(id: number): Observable<ApiResponseModel<{ templates: WhatsappTemplateModel[] }>> {
    return this.http.get<ApiResponseModel<{ templates: WhatsappTemplateModel[] }>>(`${this.baseRoute}/${id}/waba/templates`);
  }
}
