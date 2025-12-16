import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AccountApi } from '../api/account.api';
import {
  AccountDetailsModel,
  AccountModel,
  AccountStatementModel,
  AccountSubscriptionModel,
  AccountUpdateRequestModel,
  ApiMessage,
  ApiResponseModel,
  EmailOtpRequestModel,
  ForgetPasswordRequestModel,
  NewUserAccountEmailVerificationRequestModel,
  NewUserRegistrationRequestModel,
  PageableModel,
  PrePlanSwitchInvoicePayloadModel,
  ResendVerifyEmailRequestModel,
  ResetPasswordRequestModel,
  SearchResultPayloadModel,
  UserAccountUpdateSftpSettingsRequestModel,
  UserPasswordChangeRequestModel,
  UserPlanUpdateRequestModel,
  UserProfileModel,
  UserProfileUpdateRequestModel,
  WabaUpdateRequestModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';

@Injectable({
  providedIn: 'root',
})
export class AccountService implements AccountApi {
  readonly baseRoute = `account`;

  constructor(private http: ApiHttpService) {}

  register(request: NewUserRegistrationRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/register`, request);
  }

  changePassword(request: UserPasswordChangeRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/change-password`, request);
  }

  updateProfile(request: UserProfileUpdateRequestModel): Observable<ApiResponseModel<UserProfileModel>> {
    return this.http.post<ApiResponseModel<UserProfileModel>>(`${this.baseRoute}/profile`, request);
  }

  getAccount(): Observable<ApiResponseModel<AccountModel>> {
    return this.http.get<ApiResponseModel<AccountModel>>(this.baseRoute);
  }

  updateAccount(request: AccountUpdateRequestModel): Observable<ApiResponseModel<AccountModel>> {
    return this.http.post<ApiResponseModel<AccountModel>>(this.baseRoute, request);
  }

  updatePlan(request: UserPlanUpdateRequestModel): Observable<ApiResponseModel<AccountSubscriptionModel>> {
    return this.http.post<ApiResponseModel<AccountSubscriptionModel>>(`${this.baseRoute}/plan`, request);
  }

  switchPlan(request: UserPlanUpdateRequestModel): Observable<ApiResponseModel<AccountSubscriptionModel>> {
    return this.http.post<ApiResponseModel<AccountSubscriptionModel>>(`${this.baseRoute}/plan`, request);
  }

  resendVerifyEmail(request: ResendVerifyEmailRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/resend-verify-email`, request);
  }

  verifyEmail(request: NewUserAccountEmailVerificationRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/verify-email`, request);
  }

  forgetPassword(request: ForgetPasswordRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/forget-password`, request);
  }

  resetPassword(request: ResetPasswordRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/reset-password`, request);
  }

  previewPlanSwitch(
    request: UserPlanUpdateRequestModel
  ): Observable<ApiResponseModel<PrePlanSwitchInvoicePayloadModel>> {
    return this.http.post<ApiResponseModel<PrePlanSwitchInvoicePayloadModel>>(
      `${this.baseRoute}/plan?preview=1`,
      request
    );
  }

  generateEmailOtp(request: EmailOtpRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/generate-email-otp`, request);
  }

  generateActivateEmailOtp(request: EmailOtpRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/generate-email-2fa-activation-otp`, request);
  }

  getAccountSubscriptions(
    pageable: PageableModel
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AccountSubscriptionModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<AccountSubscriptionModel>>>(
      `${this.baseRoute}/subscriptions`,
      pageable
    );
  }

  updateWaba(req: WabaUpdateRequestModel): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.put<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/waba`, req);
  }

  wabaLogin(): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.get<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/waba/login`);
  }

  registerWabaWebhook(): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.get<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/waba/register-webhook`);
  }

  unregisterWabaWebhook(): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.get<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/waba/unregister-webhook`);
  }

  getStatements(
    pageable: PageableModel,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AccountStatementModel>>> {
    startDate?.setHours(0, 0, 0, 0);
    endDate?.setHours(23, 59, 59, 9999);

    return this.http.query<ApiResponseModel<SearchResultPayloadModel<AccountStatementModel>>>(
      `${this.baseRoute}/statements`,
      pageable,
      {
        startDate,
        endDate,
      }
    );
  }

  updateSftpSettings(
    request: UserAccountUpdateSftpSettingsRequestModel
  ): Observable<ApiResponseModel<AccountDetailsModel>> {
    return this.http.put<ApiResponseModel<AccountDetailsModel>>(`${this.baseRoute}/sftp`, request);
  }
}
