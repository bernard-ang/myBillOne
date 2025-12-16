import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  TwoFactorAuthorizationRequestModel,
  TwoFactorAuthUpdateRequestModel,
  UserModel,
  UserPasswordChangeRequestModel,
  UserProfileModel,
  UserProfileUpdateRequestModel,
} from '@grabbill/lib';

export abstract class ProfileApi {
  abstract changePassword(request: UserPasswordChangeRequestModel): Observable<ApiResponseModel<ApiMessage>>;

  abstract getProfile(): Observable<ApiResponseModel<UserProfileModel>>;

  abstract updateProfile(request: UserProfileUpdateRequestModel): Observable<ApiResponseModel<UserProfileModel>>;

  abstract markGuidedStepsViewed(): Observable<ApiResponseModel<UserModel>>;

  abstract updateMfa(request: TwoFactorAuthUpdateRequestModel): Observable<ApiResponseModel<UserProfileModel>>;

  abstract activateMfa(request: TwoFactorAuthorizationRequestModel): Observable<ApiResponseModel<UserProfileModel>>;

  abstract generateMfaQrcode(): Observable<Blob>;
}
