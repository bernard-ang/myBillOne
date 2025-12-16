import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel, TwoFactorAuthorizationRequestModel,
  TwoFactorAuthUpdateRequestModel,
  UserModel,
  UserPasswordChangeRequestModel,
  UserProfileModel,
  UserProfileUpdateRequestModel
} from "@grabbill/lib";
import { ProfileApi } from '../api/profile.api';
import { ApiHttpService } from './api-http.service';

@Injectable({
  providedIn: 'root',
})
export class ProfileService implements ProfileApi {
  readonly baseRoute = `profile`;

  constructor(private http: ApiHttpService) {}

  changePassword(request: UserPasswordChangeRequestModel): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.post<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/change-password`, request);
  }

  getProfile(): Observable<ApiResponseModel<UserProfileModel>> {
    return this.http.get<ApiResponseModel<UserProfileModel>>(this.baseRoute);
  }

  updateProfile(request: UserProfileUpdateRequestModel): Observable<ApiResponseModel<UserProfileModel>> {
    return this.http.post<ApiResponseModel<UserProfileModel>>(this.baseRoute, request);
  }

  markGuidedStepsViewed(): Observable<ApiResponseModel<UserModel>> {
    return this.http.put<ApiResponseModel<UserModel>>(`${this.baseRoute}/mark-guided-steps-viewed`, {});
  }

  activateMfa(request: TwoFactorAuthorizationRequestModel): Observable<ApiResponseModel<UserProfileModel>> {
    return this.http.post<ApiResponseModel<UserProfileModel>>(`${this.baseRoute}/2fa/activate`, request);
  }

  updateMfa(request: TwoFactorAuthUpdateRequestModel): Observable<ApiResponseModel<UserProfileModel>> {
    return this.http.put<ApiResponseModel<UserProfileModel>>(`${this.baseRoute}/2fa`, request);
  }

  generateMfaQrcode(): Observable<Blob> {
    return this.http.getImage<Blob>(`${this.baseRoute}/2fa/generate-qr`);
  }
}
