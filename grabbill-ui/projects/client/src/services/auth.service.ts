import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthApi } from '../api/auth.api';
import {
  ApiResponseModel,
  AuthorizationRequestModel,
  LoginRequestModel,
  TwoFactorAuthorizationRequestModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService implements AuthApi {
  readonly baseRoute = `auth`;

  constructor(private http: ApiHttpService) {}

  login(loginRequest: LoginRequestModel): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/login`, loginRequest);
  }

  authorize(loginRequest: AuthorizationRequestModel): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/authorize`, loginRequest);
  }

  getAuthority(): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.get<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/authority`);
  }

  refreshToken(): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/refresh`, {});
  }

  twoFactorAuthorization(
    request: TwoFactorAuthorizationRequestModel
  ): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/2-factor-auth`, request);
  }

  logout(): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/logout`, {});
  }
}
