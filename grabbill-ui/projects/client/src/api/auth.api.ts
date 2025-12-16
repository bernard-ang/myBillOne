import { Observable } from 'rxjs';
import {
  ApiResponseModel,
  AuthorizationRequestModel,
  LoginRequestModel,
  TwoFactorAuthorizationRequestModel,
  UserAuthorityModel,
} from '@grabbill/lib';

export abstract class AuthApi {
  abstract login(request: LoginRequestModel): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract authorize(loginRequest: AuthorizationRequestModel): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract getAuthority(): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract refreshToken(): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract twoFactorAuthorization(
    request: TwoFactorAuthorizationRequestModel
  ): Observable<ApiResponseModel<UserAuthorityModel>>;

  abstract logout(): Observable<ApiResponseModel<UserAuthorityModel>>;
}
