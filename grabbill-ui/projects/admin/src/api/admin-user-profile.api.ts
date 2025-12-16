import { Observable } from 'rxjs';
import {
  AdminUserModel,
  ApiResponseModel,
  TwoFactorAuthorizationRequestModel,
  TwoFactorAuthUpdateRequestModel,
  UserProfileModel,
} from '@grabbill/lib';

export abstract class AdminUserProfileApi {
  abstract updateMfa(request: TwoFactorAuthUpdateRequestModel): Observable<ApiResponseModel<AdminUserModel>>;

  abstract activateMfa(request: TwoFactorAuthorizationRequestModel): Observable<ApiResponseModel<UserProfileModel>>;

  abstract generateMfaQrcode(): Observable<Blob>;
}
