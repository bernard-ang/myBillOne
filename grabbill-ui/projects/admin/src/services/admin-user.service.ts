import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AdminUserModel,
  AdminUserRequestModel,
  ApiMessage,
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { ApiHttpService } from './api-http.service';
import { AdminUserApi } from '../api/admin-user.api';

@Injectable({
  providedIn: 'root',
})
export class AdminUserService implements AdminUserApi {
  readonly baseRoute = `mgmt/admin-users`;

  constructor(private http: ApiHttpService) {}

  activateUser(id: number): Observable<ApiResponseModel<AdminUserModel>> {
    return this.http.put<ApiResponseModel<AdminUserModel>>(`${this.baseRoute}/${id}/activate`, {});
  }

  deactivateUser(id: number): Observable<ApiResponseModel<AdminUserModel>> {
    return this.http.put<ApiResponseModel<AdminUserModel>>(`${this.baseRoute}/${id}/deactivate`, {});
  }

  deleteUser(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }

  getUsers(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AdminUserModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<AdminUserModel>>>(`${this.baseRoute}`, pageable, {
      name,
    });
  }

  newUser(request: AdminUserRequestModel): Observable<ApiResponseModel<AdminUserModel>> {
    return this.http.post<ApiResponseModel<AdminUserModel>>(`${this.baseRoute}`, request);
  }

  updateUser(id: number, request: AdminUserRequestModel): Observable<ApiResponseModel<AdminUserModel>> {
    return this.http.put<ApiResponseModel<AdminUserModel>>(`${this.baseRoute}/${id}`, request);
  }
}
