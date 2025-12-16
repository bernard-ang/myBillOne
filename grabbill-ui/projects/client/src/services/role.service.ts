import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiMessage, ApiResponseModel, PrivilegeModel, RoleModel, RoleRequestModel } from '@grabbill/lib';
import { RoleApi } from '../api/role.api';
import { ApiHttpService } from './api-http.service';

@Injectable({
  providedIn: 'root',
})
export class RoleService implements RoleApi {
  readonly baseRoute = `roles`;

  constructor(private http: ApiHttpService) {}

  deleteRole(id: number): Observable<ApiResponseModel<ApiMessage>> {
    return this.http.delete<ApiResponseModel<ApiMessage>>(`${this.baseRoute}/${id}`);
  }

  newRole(request: RoleRequestModel): Observable<ApiResponseModel<RoleModel>> {
    return this.http.post<ApiResponseModel<RoleModel>>(`${this.baseRoute}`, request);
  }
  updateRole(id: number, request: RoleRequestModel): Observable<ApiResponseModel<RoleModel>> {
    return this.http.put<ApiResponseModel<RoleModel>>(`${this.baseRoute}/${id}`, request);
  }

  getRolesWithPrivilege(): Observable<ApiResponseModel<{ roles: RoleModel[]; privileges: PrivilegeModel[] }>> {
    return this.http.get<ApiResponseModel<{ roles: RoleModel[]; privileges: PrivilegeModel[] }>>(
      `${this.baseRoute}?privilege=1`
    );
  }

  getRoles(): Observable<ApiResponseModel<{ roles: string[] }>> {
    return this.http.get<ApiResponseModel<{ roles: string[] }>>(this.baseRoute);
  }
}
