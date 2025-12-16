import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthApi } from '../api/auth.api';
import {
  ApiResponseModel,
  LoginRequestModel,
  TwoFactorAuthorizationRequestModel,
  UserAuthorityModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';

@Injectable({
  providedIn: 'root',
})
export class AdminAuthService implements AuthApi {
  readonly baseRoute = `auth`;

  constructor(private http: ApiHttpService) {}

  loginWithEmail(loginRequest: LoginRequestModel): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/admin/login`, loginRequest);
  }

  getAuthority(): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.get<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/admin/authority`);
  }

  refreshToken(): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/admin/refresh`, {});
  }

  twoFactorAuthorization(
    request: TwoFactorAuthorizationRequestModel
  ): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/admin/2-factor-auth`, request);
  }

  logout(): Observable<ApiResponseModel<UserAuthorityModel>> {
    return this.http.post<ApiResponseModel<UserAuthorityModel>>(`${this.baseRoute}/admin/logout`, {});
  }
}
