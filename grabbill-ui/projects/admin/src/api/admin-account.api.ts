import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  EmailOtpRequestModel,
  ForgetPasswordRequestModel,
  ResetPasswordRequestModel,
} from '@grabbill/lib';

export abstract class AdminAccountApi {
  abstract forgetPassword(request: ForgetPasswordRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract resetPassword(request: ResetPasswordRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract generateEmailOtp(request: EmailOtpRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract generateActivateEmailOtp(request: EmailOtpRequestModel): Observable<ApiResponseModel<ApiMessage>>;
}
