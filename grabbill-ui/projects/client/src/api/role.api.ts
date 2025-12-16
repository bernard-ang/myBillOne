import { Observable } from 'rxjs';
import { ApiMessage, ApiResponseModel, PrivilegeModel, RoleModel, RoleRequestModel } from "@grabbill/lib";

export abstract class RoleApi {
  abstract getRoles(): Observable<ApiResponseModel<{ roles: string[] }>>;

  abstract getRolesWithPrivilege(): Observable<ApiResponseModel<{ roles: RoleModel[]; privileges: PrivilegeModel[] }>>;

  abstract newRole(request: RoleRequestModel): Observable<ApiResponseModel<RoleModel>>;

  abstract updateRole(id: number, request: RoleRequestModel): Observable<ApiResponseModel<RoleModel>>;

  abstract deleteRole(id: number): Observable<ApiResponseModel<ApiMessage>>;
}
