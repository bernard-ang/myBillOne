import { Observable } from 'rxjs';
import {
  AdminUserModel,
  AdminUserRequestModel,
  ApiMessage,
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';

export abstract class AdminUserApi {
  abstract getUsers(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<AdminUserModel>>>;

  abstract newUser(request: AdminUserRequestModel): Observable<ApiResponseModel<AdminUserModel>>;

  abstract updateUser(id: number, request: AdminUserRequestModel): Observable<ApiResponseModel<AdminUserModel>>;

  abstract deleteUser(id: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract activateUser(id: number): Observable<ApiResponseModel<AdminUserModel>>;

  abstract deactivateUser(id: number): Observable<ApiResponseModel<AdminUserModel>>;
}
