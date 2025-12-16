import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  EmailOtpRequestModel,
  ForgetPasswordRequestModel,
  ResetPasswordRequestModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { AdminAccountApi } from '../api/admin-account.api';

@Injectable({
  providedIn: 'root',
})
export class AdminAccountService implements AdminAccountApi {
  readonly baseRoute = `mgmt/account`;

  constructor(private http: ApiHttpService) {}

  forgetPassword(request: ForgetPasswordRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/forget-password`, request);
  }

  resetPassword(request: ResetPasswordRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/reset-password`, request);
  }

  generateEmailOtp(request: EmailOtpRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/generate-email-otp`, request);
  }

  generateActivateEmailOtp(request: EmailOtpRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/generate-email-2fa-activation-otp`, request);
  }
}
