import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AdminUserModel,
  ApiResponseModel,
  TwoFactorAuthorizationRequestModel,
  TwoFactorAuthUpdateRequestModel, UserProfileModel
} from "@grabbill/lib";
import { ApiHttpService } from './api-http.service';
import { AdminUserProfileApi } from '../api/admin-user-profile.api';

@Injectable({
  providedIn: 'root',
})
export class AdminUserProfileService implements AdminUserProfileApi {
  readonly baseRoute = `mgmt/profile`;

  constructor(private http: ApiHttpService) {}

  updateMfa(request: TwoFactorAuthUpdateRequestModel): Observable<ApiResponseModel<AdminUserModel>> {
    return this.http.put<ApiResponseModel<AdminUserModel>>(`${this.baseRoute}/2fa`, request);
  }

  activateMfa(request: TwoFactorAuthorizationRequestModel): Observable<ApiResponseModel<UserProfileModel>> {
    return this.http.post<ApiResponseModel<UserProfileModel>>(`${this.baseRoute}/2fa/activate`, request);
  }

  generateMfaQrcode(): Observable<Blob> {
    return this.http.getImage<Blob>(`${this.baseRoute}/2fa/generate-qr`);
  }
}
