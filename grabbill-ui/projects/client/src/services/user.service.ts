import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
  UserModel,
  UserRequestModel,
} from '@grabbill/lib';
import { UserApi } from '../api/user.api';
import { ApiHttpService } from './api-http.service';

@Injectable({
  providedIn: 'root',
})
export class UserService implements UserApi {
  readonly baseRoute = `users`;

  constructor(private http: ApiHttpService) {}

  activateUser(id: number): Observable<ApiResponseModel<UserModel>> {
    return this.http.put<ApiResponseModel<UserModel>>(`${this.baseRoute}/${id}/activate`, {});
  }

  deactivateUser(id: number): Observable<ApiResponseModel<UserModel>> {
    return this.http.put<ApiResponseModel<UserModel>>(`${this.baseRoute}/${id}/deactivate`, {});
  }

  deleteUser(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }

  getUsers(pageable: PageableModel, name?: string): Observable<ApiResponseModel<SearchResultPayloadModel<UserModel>>> {
    return this.http.query<ApiResponseModel<SearchResultPayloadModel<UserModel>>>(`${this.baseRoute}`, pageable, {
      name,
    });
  }

  newUser(request: UserRequestModel): Observable<ApiResponseModel<UserModel>> {
    return this.http.post<ApiResponseModel<UserModel>>(`${this.baseRoute}`, request);
  }

  updateUser(id: number, request: UserRequestModel): Observable<ApiResponseModel<UserModel>> {
    return this.http.put<ApiResponseModel<UserModel>>(`${this.baseRoute}/${id}`, request);
  }

  resetPassword(id: number): Observable<ApiResponseModel<UserModel>> {
    return this.http.put<ApiResponseModel<UserModel>>(`${this.baseRoute}/${id}/reset-password`, {});
  }
}
