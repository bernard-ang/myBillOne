import { Observable } from 'rxjs';
import {
  ApiResponseModel,
  LoginRequestModel,
  TwoFactorAuthorizationRequestModel,
  UserAuthorityModel,
} from '@grabbill/lib';

export abstract class AuthApi {
  abstract loginWithEmail(request: LoginRequestModel): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract getAuthority(): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract refreshToken(): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract twoFactorAuthorization(
    request: TwoFactorAuthorizationRequestModel
  ): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract logout(): Observable<ApiResponseModel<UserAuthorityModel>>;
}
