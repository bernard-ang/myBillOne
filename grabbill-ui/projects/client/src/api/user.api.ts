import { Observable } from 'rxjs';
import {
  ApiMessage,
  ApiResponseModel,
  PageableModel,
  SearchResultPayloadModel,
  UserModel,
  UserRequestModel,
} from '@grabbill/lib';

export abstract class UserApi {
  abstract getUsers(
    pageable: PageableModel,
    name?: string
  ): Observable<ApiResponseModel<SearchResultPayloadModel<UserModel>>>;

  abstract newUser(request: UserRequestModel): Observable<ApiResponseModel<UserModel>>;

  abstract updateUser(id: number, request: UserRequestModel): Observable<ApiResponseModel<UserModel>>;

  abstract deleteUser(id: number): Observable<ApiResponseModel<ApiMessage>>;

  abstract activateUser(id: number): Observable<ApiResponseModel<UserModel>>;

  abstract deactivateUser(id: number): Observable<ApiResponseModel<UserModel>>;

  abstract resetPassword(id: number): Observable<ApiResponseModel<UserModel>>;
}
