import { Observable } from 'rxjs';
import {
  AccountDetailsModel,
  AccountModel,
  AccountSubscriptionModel,
  AccountUpdateRequestModel,
  WabaUpdateRequestModel,
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
  UserPasswordChangeRequestModel,
  UserPlanUpdateRequestModel,
  UserProfileModel,
  UserProfileUpdateRequestModel,
  AccountStatementModel, UserAccountUpdateSftpSettingsRequestModel
} from "@grabbill/lib";

export abstract class AccountApi {
  abstract register(request: NewUserRegistrationRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract changePassword(request: UserPasswordChangeRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract updateProfile(request: UserProfileUpdateRequestModel): Observable<ApiResponseModel<UserProfileModel>>;

  abstract getAccount(): Observable<ApiResponseModel<AccountModel>>;

  abstract updateAccount(request: AccountUpdateRequestModel): Observable<ApiResponseModel<AccountModel>>;

  abstract updatePlan(request: UserPlanUpdateRequestModel): Observable<ApiResponseModel<AccountSubscriptionModel>>;

  abstract previewPlanSwitch(
    request: UserPlanUpdateRequestModel
  ): Observable<ApiResponseModel<PrePlanSwitchInvoicePayloadModel>>;

  abstract switchPlan(request: UserPlanUpdateRequestModel): Observable<ApiResponseModel<AccountSubscriptionModel>>;

  abstract resendVerifyEmail(request: ResendVerifyEmailRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract verifyEmail(request: NewUserAccountEmailVerificationRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract forgetPassword(request: ForgetPasswordRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract resetPassword(request: ResetPasswordRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract generateEmailOtp(request: EmailOtpRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract generateActivateEmailOtp(request: EmailOtpRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract getAccountSubscriptions(
    pageable: PageableModel
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AccountSubscriptionModel>>>;

  abstract updateWaba(req: WabaUpdateRequestModel): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract wabaLogin(): Observable<ApiResponseModel<ApiMessage>>;

  abstract registerWabaWebhook(): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract unregisterWabaWebhook(): Observable<ApiResponseModel<AccountDetailsModel>>;

  abstract getStatements(
    pageable: PageableModel,
    startDate?: Date,
    endDate?: Date
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AccountStatementModel>>>;

  abstract updateSftpSettings(request: UserAccountUpdateSftpSettingsRequestModel): Observable<ApiResponseModel<AccountDetailsModel>>;
}
